package com.ximad.prism.service;

import com.ximad.prism.config.model.ConditionalStageConfig;
import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.PluginContext;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IRerankStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RerankService {

    private final Map<String, IRerankStrategy> strategies;

    public void applyStrategy(RecContext ctx) {
        List<RecommendItem> current = ctx.getCandidates();
        if (current == null || current.isEmpty()) return;
        
        List<ConditionalStageConfig> configs = ctx.getConfig().rerank();
        Map<String, String> assignments = ctx.getExpConfig() != null 
            ? ctx.getExpConfig().getExperimentAssignments() 
            : Collections.emptyMap();
        
        List<StepConfig> steps = ConditionalStageConfig.findMatchingSteps(configs, assignments);
        
        for (StepConfig step : steps) {
            IRerankStrategy strategy = strategies.get(step.name());
            if (strategy != null) {
                List<RecommendItem> next = ScopedValue.where(PluginContext.CURRENT_PLUGIN_NAME, step.name()).call(() -> {
                    long start = System.currentTimeMillis();
                    List<RecommendItem> processed = strategy.process(ctx, ctx.getCandidates(), step);
                    long latency = System.currentTimeMillis() - start;
                    
                    ctx.getTrace().recordPlugin(PipelineStage.RERANK, step.name(), latency, processed.size());
                    return processed;
                });
                current = next;
                ctx.setCandidates(current);
            }
        }
    }
}
