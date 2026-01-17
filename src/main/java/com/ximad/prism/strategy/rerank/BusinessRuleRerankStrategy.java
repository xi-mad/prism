package com.ximad.prism.strategy.rerank;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IRerankStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 业务规则重排策略 - 例如：强制某些置顶、提权置顶、特定来源提权
 */
@Component("BusinessRuleRerank")
public class BusinessRuleRerankStrategy implements IRerankStrategy {
    @Override
    public List<RecommendItem> process(RecContext ctx, List<RecommendItem> items, StepConfig config) {
        List<RecommendItem> result = new ArrayList<>(items);
        
        // 模拟置顶项 (例如广告或运营推荐)
        String boostId = (String) config.params().get("boost_id");
        if (boostId != null) {
            result.stream()
                .filter(i -> i.getId().equals(boostId))
                .findFirst()
                .ifPresent(item -> {
                    result.remove(item);
                    result.add(0, item);
                    item.addReason("BOOST", "Operation forced top");
                });
        }
        
        return result;
    }
}
