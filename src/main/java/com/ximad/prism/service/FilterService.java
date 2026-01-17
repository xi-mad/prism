package com.ximad.prism.service;

import com.ximad.prism.config.model.ConditionalStageConfig;
import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.PluginContext;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.FilterResult;
import com.ximad.prism.plugin.IFilterStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilterService {

    private final Map<String, IFilterStrategy> strategies;

    public void applyFilters(RecContext ctx) {
        List<RecommendItem> current = ctx.getCandidates();
        if (current == null || current.isEmpty()) return;
        
        List<ConditionalStageConfig> configs = ctx.getConfig().filters();
        Map<String, String> assignments = ctx.getExpConfig() != null 
            ? ctx.getExpConfig().getExperimentAssignments() 
            : Collections.emptyMap();
        
        List<StepConfig> steps = ConditionalStageConfig.findMatchingSteps(configs, assignments);
        
        for (StepConfig step : steps) {
            IFilterStrategy strategy = strategies.get(step.name());
            if (strategy != null) {
                List<RecommendItem> finalCurrent = current;
                List<RecommendItem> next = ScopedValue.where(PluginContext.CURRENT_PLUGIN_NAME, step.name()).call(() -> {
                    long start = System.currentTimeMillis();
                    List<RecommendItem> filtered = new ArrayList<>();
                    for (RecommendItem item : finalCurrent) {
                        FilterResult result = strategy.filter(ctx, item, step);
                        if (result.passed()) {
                            filtered.add(item);
                        }
                    }
                    long latency = System.currentTimeMillis() - start;
                    ctx.getTrace().recordPlugin(PipelineStage.FILTER, step.name(), latency, filtered.size());
                    return filtered;
                });
                
                current = next;
                if (current.isEmpty()) break;
            }
        }
        ctx.setCandidates(current);
    }
}
