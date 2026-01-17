package com.ximad.prism.strategy.rerank;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IRerankStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 截断重排策略 - 根据配置的条数对结果集进行截断
 */
@Component("TruncateRerank")
public class TruncateRerankStrategy implements IRerankStrategy {
    @Override
    public List<RecommendItem> process(RecContext ctx, List<RecommendItem> items, StepConfig config) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        // 从配置参数中获取截断限制，默认为 20 条
        int limit = (int) config.params().getOrDefault("limit", 20);

        // 执行截断
        List<RecommendItem> result = items.stream()
                .limit(limit)
                .collect(Collectors.toList());
        List<String> removed = items.stream().skip(limit).map(RecommendItem::getId).toList();

        ctx.getTrace().log(PipelineStage.RERANK, "remove ids: {}", removed);

        return result;
    }
}
