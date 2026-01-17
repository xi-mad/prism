package com.ximad.prism.strategy.recall;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IRecallStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 热门召回策略 - 模拟获取全网最热门的内容
 */
@Component("HotRecall")
public class HotRecallStrategy implements IRecallStrategy {
    @Override
    public List<RecommendItem> recall(RecContext ctx, StepConfig config) {
        int count = (int) config.params().getOrDefault("count", 50);
        List<RecommendItem> items = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            RecommendItem item = new RecommendItem("hot-item-" + i, "JOB");
            item.setScore(0.9 - (i * 0.001)); // 模拟热门分
            item.addReason("HOT", "全网最热门职位 #" + (i + 1));
            item.getFeatures().put("click_count", 10000 - i * 100);
            items.add(item);
        }
        
        return items;
    }
}
