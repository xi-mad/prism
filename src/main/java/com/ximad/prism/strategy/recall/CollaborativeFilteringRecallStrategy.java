package com.ximad.prism.strategy.recall;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IRecallStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 协同过滤召回策略 - 模拟“看过这个职位的人也看了”或“相似兴趣用户”推荐
 */
@Component("CFRecall")
public class CollaborativeFilteringRecallStrategy implements IRecallStrategy {
    @Override
    public List<RecommendItem> recall(RecContext ctx, StepConfig config) {
        int count = (int) config.params().getOrDefault("count", 40);
        List<RecommendItem> items = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            RecommendItem item = new RecommendItem("cf-item-" + i, "JOB");
            item.setScore(0.85 - (i * 0.005));
            item.addReason("CF", "基于您感兴趣的类似职位推荐");
            item.getFeatures().put("similarity", 0.95 - i * 0.01);
            items.add(item);
        }
        
        return items;
    }
}
