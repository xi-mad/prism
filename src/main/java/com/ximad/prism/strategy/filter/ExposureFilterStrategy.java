package com.ximad.prism.strategy.filter;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.FilterResult;
import com.ximad.prism.plugin.IFilterStrategy;
import org.springframework.stereotype.Component;

/**
 * 实际曝光过滤器 - 检查物料是否在 context 的曝光历史中
 */
@Component("ExposureFilter")
public class ExposureFilterStrategy implements IFilterStrategy {
    @Override
    public FilterResult filter(RecContext ctx, RecommendItem item, StepConfig config) {
        // 从 Context 中获取拦截器预加载好的历史记录
        if (ctx.getExposureHistory() != null && ctx.getExposureHistory().contains(item.getId())) {
            // 自动记录日志到本插件
            ctx.getTrace().log(PipelineStage.FILTER, 
                "Item [{}] filtered out due to previous exposure in this session/cache", item.getId());
            return new FilterResult(false, "Already Exposed");
        }
        
        return new FilterResult(true, null);
    }
}
