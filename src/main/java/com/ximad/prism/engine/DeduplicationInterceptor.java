package com.ximad.prism.engine;

import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IPipelineInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 召回去重与合并拦截器
 */
@Slf4j
@Component("Deduplication")
@Order(100)
public class DeduplicationInterceptor implements IPipelineInterceptor {

    @Override
    public void postExecute(PipelineStage stage, RecContext ctx) {
        if (stage != PipelineStage.RECALL) {
            return;
        }

        List<RecommendItem> rawItems = ctx.getCandidates();
        if (rawItems == null || rawItems.isEmpty()) {
            return;
        }

        Map<String, RecommendItem> mergedMap = new HashMap<>();
        for (RecommendItem item : rawItems) {
            mergedMap.merge(item.getId(), item, (existing, replacement) -> {
                existing.getReasons().addAll(replacement.getReasons());
                existing.setScore(Math.max(existing.getScore(), replacement.getScore()));
                existing.getFeatures().putAll(replacement.getFeatures());
                return existing;
            });
        }

        List<RecommendItem> uniqueItems = new ArrayList<>(mergedMap.values());
        ctx.setCandidates(uniqueItems);
    }
}
