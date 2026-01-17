package com.ximad.prism.strategy.recall;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IRecallStrategy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import com.ximad.prism.core.context.RecContext;

@Component("UserInterestRecall")
public class UserInterestRecallStrategy implements IRecallStrategy {
    @Override
    public List<RecommendItem> recall(RecContext ctx, StepConfig config) {
        // Mock RPC Delay
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        List<RecommendItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) {
            RecommendItem item = new RecommendItem("interest-item-" + i, "JOB");
            item.addReason("INTEREST", "From UserInterestRecall #" + i);
            items.add(item);
        }
        return items;
    }
}
