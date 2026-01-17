package com.ximad.prism.service;

import com.ximad.prism.config.model.ConditionalStageConfig;
import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.PluginContext;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IHydrationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class HydrationService {

    private final Map<String, IHydrationStrategy> strategies;

    public List<RecommendItem> fillAndRecord(RecContext ctx) {
        List<RecommendItem> items = ctx.getCandidates();
        if (items == null || items.isEmpty()) return Collections.emptyList();
        
        List<ConditionalStageConfig> configs = ctx.getConfig().hydration();
        Map<String, String> assignments = ctx.getExpConfig() != null 
            ? ctx.getExpConfig().getExperimentAssignments() 
            : Collections.emptyMap();
        
        List<StepConfig> steps = ConditionalStageConfig.findMatchingSteps(configs, assignments);
        
        for (StepConfig step : steps) {
            IHydrationStrategy strategy = strategies.get(step.name());
            if (strategy != null) {
                items = ScopedValue.where(PluginContext.CURRENT_PLUGIN_NAME, step.name()).call(() -> {
                    long start = System.currentTimeMillis();
                    List<RecommendItem> hydrated = strategy.hydrate(ctx, ctx.getCandidates(), step);
                    long latency = System.currentTimeMillis() - start;
                    ctx.getTrace().recordPlugin(PipelineStage.HYDRATION, step.name(), latency, hydrated.size());
                    return hydrated;
                });
                ctx.setCandidates(items);
            }
        }
        return items;
    }
}
