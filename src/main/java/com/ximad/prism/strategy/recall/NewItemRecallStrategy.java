package com.ximad.prism.strategy.recall;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IRecallStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 新品召回策略 - 模拟获取最新发布的职位
 */
@Component("NewItemRecall")
public class NewItemRecallStrategy implements IRecallStrategy {
    @Override
    public List<RecommendItem> recall(RecContext ctx, StepConfig config) {
        int count = (int) config.params().getOrDefault("count", 30);
        List<RecommendItem> items = new ArrayList<>();
        
        long now = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            RecommendItem item = new RecommendItem("new-item-" + i, "JOB");
            item.setScore(0.8 - (i * 0.01));
            item.addReason("NEW", "最新发布");
            item.getFeatures().put("publish_time", now - (long) i * 3600000); // 每小时一个
            items.add(item);
        }
        
        return items;
    }
}
