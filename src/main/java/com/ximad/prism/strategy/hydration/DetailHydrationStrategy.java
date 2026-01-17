package com.ximad.prism.strategy.hydration;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IHydrationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 详情补全策略 - 聚合推荐结果，分业务线并行获取详细信息（标题、图片、薪资等）
 */
@Component("DetailHydration")
@Slf4j
public class DetailHydrationStrategy implements IHydrationStrategy {
    @Override
    public List<RecommendItem> hydrate(RecContext ctx, List<RecommendItem> items, StepConfig config) {
        if (items.isEmpty()) return items;

        log.info("Hydrating {} items with detailed info", items.size());
        
        // 模拟批量调用下游服务补全数据
        for (RecommendItem item : items) {
            Map<String, Object> display = item.getDisplayData();
            display.put("title", "Mock Title for " + item.getId());
            display.put("sub_title", "Mock subtitle content");
            
            if ("JOB".equals(item.getBizType())) {
                display.put("salary", "20k-30k");
                display.put("company", "Mock Tech Co., Ltd.");
            }
        }
        
        return items;
    }
}
