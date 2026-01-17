package com.ximad.prism.service;

import com.ximad.prism.config.model.ConditionalStageConfig;
import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.PluginContext;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IRecallStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.StructuredTaskScope;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecallService {

    private final Map<String, IRecallStrategy> strategies;

    public void parallelRecall(RecContext ctx) {
        ConcurrentLinkedQueue<RecommendItem> collected = new ConcurrentLinkedQueue<>();
        
        List<ConditionalStageConfig> configs = ctx.getConfig().recall();
        Map<String, String> expAssignments = ctx.getExpConfig() != null 
            ? ctx.getExpConfig().getExperimentAssignments() 
            : Collections.emptyMap();
        
        List<StepConfig> steps = ConditionalStageConfig.findMatchingSteps(configs, expAssignments);
        if (steps.isEmpty()) return;

        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {
            for (StepConfig step : steps) {
                IRecallStrategy strategy = strategies.get(step.name());
                if (strategy != null) {
                    scope.fork(() -> {
                        // 【核心改动】：绑定当前插件名称到作用域
                        return ScopedValue.where(PluginContext.CURRENT_PLUGIN_NAME, step.name()).call(() -> {
                            long start = System.currentTimeMillis();
                            List<RecommendItem> items = strategy.recall(ctx, step);
                            long latency = System.currentTimeMillis() - start;

                            ctx.getTrace().recordPlugin(PipelineStage.RECALL, step.name(), latency, items.size());
                            collected.addAll(items);
                            return null;
                        });
                    });
                }
            }
            scope.join();
        } catch (Exception e) {
            log.error("Recall Error", e);
        }

        ctx.setCandidates(new ArrayList<>(collected));
    }
}
