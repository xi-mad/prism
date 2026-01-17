package com.ximad.prism.strategy.filter;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.FilterResult;
import com.ximad.prism.plugin.IFilterStrategy;
import org.springframework.stereotype.Component;

@Component("CityFilter")
public class CityFilterStrategy implements IFilterStrategy {
    @Override
    public FilterResult filter(RecContext ctx, RecommendItem item, StepConfig config) {
        String userCity = ctx.getUserProfile().getLocation();
        String itemCity = (String) item.getFeatures().getOrDefault("city", userCity); // 默认匹配

        if (userCity != null && !userCity.equals("Unknown") && !userCity.equals(itemCity)) {
            // 如果配置了强制匹配城市
            boolean forceMatch = (boolean) config.params().getOrDefault("force_match", false);
            if (forceMatch) {
                return new FilterResult(false, "City mismatch: user=" + userCity + ", item=" + itemCity);
            }
        }
        return new FilterResult(true, null);
    }
}
