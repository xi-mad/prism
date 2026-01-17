package com.ximad.prism.engine;

import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IPipelineInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 性能与漏斗追踪拦截器 - 自动记录阶段出入 ID
 */
@Slf4j
@Component("Performance")
@Order(0)
public class PerformanceInterceptor implements IPipelineInterceptor {

    // 记录每个线程视角下的阶段输入 ID 列表
    private final Map<Long, Map<PipelineStage, List<String>>> inputSnapshot = new ConcurrentHashMap<>();

    @Override
    public void preExecute(PipelineStage stage, RecContext ctx) {
        ctx.getTrace().startStage(stage);
        
        // 抓取输入 ID
        List<String> inIds = ctx.getCandidates() == null ? Collections.emptyList() :
                ctx.getCandidates().stream().map(RecommendItem::getId).collect(Collectors.toList());
        
        long threadId = Thread.currentThread().threadId();
        inputSnapshot.computeIfAbsent(threadId, k -> new ConcurrentHashMap<>()).put(stage, inIds);
    }

    @Override
    public void postExecute(PipelineStage stage, RecContext ctx) {
        long threadId = Thread.currentThread().threadId();
        List<String> inIds = inputSnapshot.getOrDefault(threadId, Collections.emptyMap())
                                         .getOrDefault(stage, Collections.emptyList());
        
        // 抓取输出 ID
        List<String> outIds = ctx.getCandidates() == null ? Collections.emptyList() :
                ctx.getCandidates().stream().map(RecommendItem::getId).collect(Collectors.toList());

        ctx.getTrace().endStage(stage, inIds, outIds);
        
        // 清理缓存
        if (stage == PipelineStage.HYDRATION) {
            inputSnapshot.remove(threadId);
        }
    }
}
