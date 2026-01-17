package com.ximad.prism.strategy.filter;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.FilterResult;
import com.ximad.prism.plugin.IFilterStrategy;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 黑名单过滤器 - 过滤用户拉黑的公司或职位
 */
@Component("BlacklistFilter")
public class BlacklistFilterStrategy implements IFilterStrategy {
    @Override
    public FilterResult filter(RecContext ctx, RecommendItem item, StepConfig config) {
        // 从用户画像中获取黑名单
        Set<String> negativeFeedback = ctx.getUserProfile().getNegativeFeedback();
        if (negativeFeedback != null && negativeFeedback.contains(item.getId())) {
            return new FilterResult(false, "In user blacklist");
        }
        
        return new FilterResult(true, null);
    }
}
