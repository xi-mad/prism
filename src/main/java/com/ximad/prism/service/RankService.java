package com.ximad.prism.service;

import com.ximad.prism.config.model.ConditionalStageConfig;
import com.ximad.prism.config.model.RankConfig;
import com.ximad.prism.core.context.PluginContext;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IRankStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankService {

    private final Map<String, IRankStrategy> strategies;

    public void multiObjectiveRank(RecContext ctx) {
        List<ConditionalStageConfig> configs = ctx.getConfig().ranking();
        Map<String, String> expAssignments = ctx.getExpConfig() != null 
            ? ctx.getExpConfig().getExperimentAssignments() 
            : Collections.emptyMap();
        
        ConditionalStageConfig matchedConfig = ConditionalStageConfig.findMatchingRankConfig(configs, expAssignments);
        if (matchedConfig == null || matchedConfig.modelId() == null) return;
        
        String modelId = matchedConfig.modelId();
        RankConfig config = new RankConfig(modelId, matchedConfig.fusionFormula(), matchedConfig.calibration());
        IRankStrategy strategy = strategies.get(modelId);

        if (strategy != null) {
            ScopedValue.where(PluginContext.CURRENT_PLUGIN_NAME, modelId).run(() -> {
                long start = System.currentTimeMillis();
                strategy.rank(ctx, config);
                long latency = System.currentTimeMillis() - start;

                List<RecommendItem> items = ctx.getCandidates();
                if (items != null) {
                    ctx.getTrace().recordPlugin(PipelineStage.RANK, modelId, latency, items.size());
                }
            });
        }
    }
}
