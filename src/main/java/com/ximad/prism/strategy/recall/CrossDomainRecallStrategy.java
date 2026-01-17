package com.ximad.prism.strategy.recall;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IRecallStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

import com.ximad.prism.core.context.RecContext;

@Component("CrossDomainRecall")
public class CrossDomainRecallStrategy implements IRecallStrategy {
    @Override
    public List<RecommendItem> recall(RecContext ctx, StepConfig config) {
        // Get target biz from config params "target_biz"
        String targetBiz = (String) config.params().getOrDefault("target_biz", "HOUSE");
        
        List<RecommendItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RecommendItem item = new RecommendItem(targetBiz.toLowerCase() + "-cross-" + i, targetBiz);
            item.addReason("CROSS", "From CrossDomainRecall #" + i);
            items.add(item);
        }
        return items;
    }
}
