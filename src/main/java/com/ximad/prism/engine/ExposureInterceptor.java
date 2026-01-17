package com.ximad.prism.engine;

import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IPipelineInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 曝光拦截器 - 负责读取历史曝光和记录新的曝光
 */
@Slf4j
@Component("Exposure")
@Order(10) // 在 Performance 之后执行
public class ExposureInterceptor implements IPipelineInterceptor {

    @Override
    public void preExecute(PipelineStage stage, RecContext ctx) {
        // 在进入画像载入或过滤阶段前加载曝光历史
        if (stage == PipelineStage.PROFILE) {
            String userId = ctx.getRequest().userId();
            // 模拟从缓存加载：alice 看过 item-1, bob 看过 item-2
            List<String> mockHistory = "alice".equals(userId) ? 
                    Arrays.asList("item-1") : Arrays.asList("item-2");
            
            ctx.setExposureHistory(mockHistory);
            ctx.getTrace().log(stage, "Loaded {} exposure records for user: {}", mockHistory.size(), userId);
        }
    }

    @Override
    public void postExecute(PipelineStage stage, RecContext ctx) {
        // 在流水线最后（补全阶段后）记录本次下发的物料
        if (stage == PipelineStage.HYDRATION) {
            List<RecommendItem> finalItems = ctx.getCandidates();
            if (finalItems == null || finalItems.isEmpty()) return;

            List<String> idsToRecord = finalItems.stream()
                    .map(RecommendItem::getId)
                    .collect(Collectors.toList());

            // 模拟记录到缓存
            log.info("[Exposure] Recording {} items for user {}: {}", 
                    idsToRecord.size(), ctx.getRequest().userId(), idsToRecord);
            
            ctx.getTrace().log(stage, "Successfully recorded {} items to exposure cache", idsToRecord.size());
        }
    }
}
