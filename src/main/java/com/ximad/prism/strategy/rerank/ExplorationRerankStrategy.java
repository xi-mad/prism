package com.ximad.prism.strategy.rerank;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IRerankStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("ExplorationRerank")
public class ExplorationRerankStrategy implements IRerankStrategy {
    @Override
    public List<RecommendItem> process(RecContext ctx, List<RecommendItem> items, StepConfig config) {
        // E&E - Insert exploration item
        List<RecommendItem> result = new ArrayList<>(items);
        
        RecommendItem exploreItem = new RecommendItem("explore-" + System.nanoTime(), "JOB");
        exploreItem.addReason("EXPLORE", "Discover New World");
        exploreItem.setScore(999.0);
        
        // Insert at pos specified in config, default 4
        int pos = (int) config.params().getOrDefault("insert_pos", 4);
        if (result.size() >= pos) {
            result.add(pos, exploreItem);
            ctx.getTrace().log(PipelineStage.RERANK, "Insert exploration item at pos {}", pos);
        } else {
            result.add(exploreItem);
            ctx.getTrace().log(PipelineStage.RERANK, "Insert exploration item at end");
        }

        return result;
    }
}
