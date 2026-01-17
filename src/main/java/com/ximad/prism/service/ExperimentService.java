package com.ximad.prism.service;

import com.ximad.prism.config.model.ConditionalStageConfig;
import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.PluginContext;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IExperimentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExperimentService {

    private final Map<String, IExperimentStrategy> strategies;

    public void shunt(RecContext ctx) {
        List<ConditionalStageConfig> configs = ctx.getConfig().experiment();
        Map<String, String> assignments = ctx.getExpConfig() != null 
            ? ctx.getExpConfig().getExperimentAssignments() 
            : Collections.emptyMap();
        
        List<StepConfig> steps = ConditionalStageConfig.findMatchingSteps(configs, assignments);
        
        for (StepConfig step : steps) {
            IExperimentStrategy strategy = strategies.get(step.name());
            if (strategy != null) {
                ScopedValue.where(PluginContext.CURRENT_PLUGIN_NAME, step.name()).run(() -> {
                    long start = System.currentTimeMillis();
                    strategy.shunt(ctx, step);
                    long latency = System.currentTimeMillis() - start;
                    ctx.getTrace().recordPlugin(PipelineStage.EXPERIMENT, step.name(), latency, 0);
                });
            }
        }
    }
}
