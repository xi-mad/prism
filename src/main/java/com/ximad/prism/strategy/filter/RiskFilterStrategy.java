package com.ximad.prism.strategy.filter;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.FilterResult;
import com.ximad.prism.plugin.IFilterStrategy;
import org.springframework.stereotype.Component;

/**
 * 风险控制过滤器 - 过滤掉有风险（如欺诈、异常状态）的职位
 */
@Component("RiskFilter")
public class RiskFilterStrategy implements IFilterStrategy {
    @Override
    public FilterResult filter(RecContext ctx, RecommendItem item, StepConfig config) {
        // 模拟风险检查逻辑
        Boolean isRisk = (Boolean) item.getFeatures().getOrDefault("is_risk", false);
        if (isRisk) {
            return new FilterResult(false, "Risk content detected");
        }
        
        // 模拟随机风险（用于演示）
        if (item.getId().hashCode() % 100 < 5) {
            return new FilterResult(false, "Random risk check failed");
        }
        
        return new FilterResult(true, null);
    }
}
