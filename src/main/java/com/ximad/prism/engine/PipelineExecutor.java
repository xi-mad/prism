package com.ximad.prism.engine;

import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.context.RecScope;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IPipelineInterceptor;
import com.ximad.prism.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class PipelineExecutor {

    private final ExperimentService shunting;
    private final ProfileService profiling;
    private final RecallService recalling;
    private final FilterService filtering;
    private final RankService ranking;
    private final RerankService reranking;
    private final HydrationService hydrating;
    
    // Spring 会将 Bean Name 作为 Map 的 Key 注入
    private final Map<String, IPipelineInterceptor> interceptorMap;

    public List<RecommendItem> execute() {
        RecContext ctx = RecScope.CTX.get();
        ctx.getTrace().log("Starting Pipeline Execution for User: " + ctx.getRequest().userId());
        log.info("Starting Pipeline Execution for User: {}", ctx.getRequest().userId());

        // 获取当前场景配置激活的拦截器实例，并按顺序排列（如果需要可以引进 Order 排序）
        List<IPipelineInterceptor> activeInterceptors = getActiveInterceptors(ctx);

        runStage(PipelineStage.EXPERIMENT, ctx, activeInterceptors, () -> shunting.shunt(ctx));
        runStage(PipelineStage.PROFILE, ctx, activeInterceptors, () -> profiling.loadProfile(ctx));
        runStage(PipelineStage.RECALL, ctx, activeInterceptors, () -> recalling.parallelRecall(ctx));
        runStage(PipelineStage.FILTER, ctx, activeInterceptors, () -> filtering.applyFilters(ctx));
        runStage(PipelineStage.RANK, ctx, activeInterceptors, () -> ranking.multiObjectiveRank(ctx));
        runStage(PipelineStage.RERANK, ctx, activeInterceptors, () -> reranking.applyStrategy(ctx));
        List<RecommendItem> result = runStageWithResult(PipelineStage.HYDRATION, ctx, activeInterceptors, () -> hydrating.fillAndRecord(ctx));

        log.info("Pipeline Execution Completed. Result Size: {}", result.size());
        ctx.getTrace().log("Pipeline Execution Completed. Result Size: " + result.size());
        return result;
    }

    private List<IPipelineInterceptor> getActiveInterceptors(RecContext ctx) {
        List<String> activeNames = ctx.getConfig().interceptors();
        if (activeNames == null || activeNames.isEmpty() || interceptorMap == null) {
            return Collections.emptyList();
        }
        List<IPipelineInterceptor> filtered = activeNames.stream()
                .map(interceptorMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        // 按 @Order 或 Ordered 接口排序
        AnnotationAwareOrderComparator.sort(filtered);
        return filtered;
    }

    private void runStage(PipelineStage stage, RecContext ctx, List<IPipelineInterceptor> currentInterceptors, Runnable task) {
        currentInterceptors.forEach(i -> i.preExecute(stage, ctx));
        try {
            Integer timeout = getTimeout(stage, ctx);
            if (timeout != null && timeout > 0) {
                try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll(), 
                        cfg -> cfg.withTimeout(Duration.ofMillis(timeout)))) {
                    scope.fork(() -> {
                        task.run();
                        return null;
                    });
                    scope.join();
                } catch (Exception e) {
                    log.error("Stage {} execution failed or timed out after {}ms", stage, timeout, e);
                }
            } else {
                task.run();
            }
        } finally {
            currentInterceptors.forEach(i -> i.postExecute(stage, ctx));
        }
    }

    private <T> T runStageWithResult(PipelineStage stage, RecContext ctx, List<IPipelineInterceptor> currentInterceptors, java.util.function.Supplier<T> task) {
        currentInterceptors.forEach(i -> i.preExecute(stage, ctx));
        try {
            Integer timeout = getTimeout(stage, ctx);
            if (timeout != null && timeout > 0) {
                try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll(), 
                        cfg -> cfg.withTimeout(Duration.ofMillis(timeout)))) {
                    var subtask = scope.fork(task::get);
                    scope.join();
                    return subtask.get();
                } catch (Exception e) {
                    log.error("Stage {} execution failed or timed out after {}ms", stage, timeout, e);
                    return null;
                }
            } else {
                return task.get();
            }
        } finally {
            currentInterceptors.forEach(i -> i.postExecute(stage, ctx));
        }
    }

    private Integer getTimeout(PipelineStage stage, RecContext ctx) {
        if (ctx.getConfig() == null || ctx.getConfig().timeouts() == null) {
            return null;
        }
        return ctx.getConfig().timeouts().get(stage.name());
    }
}
