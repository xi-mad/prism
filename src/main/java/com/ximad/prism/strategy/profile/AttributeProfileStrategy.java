package com.ximad.prism.strategy.profile;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.UserProfile;
import com.ximad.prism.plugin.IProfileStrategy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * 属性画像策略 - 加载用户的基本属性（城市、意向、黑名单等）
 */
@Component("AttributeProfile")
public class AttributeProfileStrategy implements IProfileStrategy {
    @Override
    public void load(RecContext ctx, StepConfig config) {
        String userId = ctx.getRequest().userId();
        
        // 模拟从 Redis/HBase 加载用户静态属性
        UserProfile profile = new UserProfile(
            Collections.singletonList("Tech"), // 兴趣标签
            new float[]{0.5f, 0.8f},           // 向量
            Set.of("hated-job-123"),           // 负反馈
            Set.of("INTENT_JOB_SEEKING"),      // 意图
            "Shanghai"                         // 所在城市
        );
        
        ctx.setUserProfile(profile);
    }
}
