package com.ximad.prism.strategy.recall;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IRecallStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

import com.ximad.prism.core.context.RecContext;

@Component("VectorRecall")
public class VectorRecallStrategy implements IRecallStrategy {
    @Override
    public List<RecommendItem> recall(RecContext ctx, StepConfig config) {
        // Mock VectorDB Search
        List<RecommendItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            RecommendItem item = new RecommendItem("vector-item-" + i, "JOB");
            item.addReason("FUZZY", "From VectorRecall #" + i);
            items.add(item);
        }
        return items;
    }
}
