package com.ximad.prism.strategy.rerank;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IRerankStrategy;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("DiversityRerank")
public class DiversityRerankStrategy implements IRerankStrategy {
    @Override
    public List<RecommendItem> process(RecContext ctx, List<RecommendItem> items, StepConfig config) {
        List<RecommendItem> result = new ArrayList<>();
        Set<String> window = new HashSet<>();
        
        for (RecommendItem item : items) {
             if (Collections.frequency(new ArrayList<>(window), item.getBizType()) >= 2) {
                ctx.getTrace().log(PipelineStage.RERANK, "biz type frequency is >= 2, continue");
                continue; 
            }
            result.add(item);
            window.add(item.getBizType());
            if (window.size() > 3) {
                 window.remove(window.iterator().next());
                 ctx.getTrace().log(PipelineStage.RERANK, "Remove item %s from window", window.iterator().next());
            }
        }
        return result;
    }
}
