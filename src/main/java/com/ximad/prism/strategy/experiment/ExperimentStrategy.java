package com.ximad.prism.strategy.experiment;

import com.ximad.prism.config.model.ExperimentDef;
import com.ximad.prism.config.model.ScenarioConfig;
import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.ExpConfig;
import com.ximad.prism.engine.PipelineStage;
import com.ximad.prism.plugin.IExperimentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实验分流策略实现 - 基于 1000 桶哈希算法
 */
@Component("ExperimentStrategy")
@Slf4j
public class ExperimentStrategy implements IExperimentStrategy {

    private static final int TOTAL_BUCKETS = 1000;

    @Override
    public void shunt(RecContext ctx, StepConfig config) {
        String userId = ctx.getRequest().userId();
        if (userId == null) {
            userId = "guest";
        }

        ScenarioConfig scenarioConfig = ctx.getConfig();
        List<ExperimentDef> experiments = scenarioConfig.experiments();

        if (experiments == null || experiments.isEmpty()) {
            log.debug("No experiments defined in config, skipping shunting");
            ctx.setExpConfig(new ExpConfig());
            return;
        }

        Map<String, String> assignments = new HashMap<>();

        for (ExperimentDef exp : experiments) {
            String variantName = shuntUser(userId, exp);
            assignments.put(exp.id(), variantName);

            ctx.getTrace().log(PipelineStage.EXPERIMENT, "User [{}] assigned to Experiment [{}], Variant [{}]", userId, exp.id(), variantName);
        }

        ExpConfig expConfig = new ExpConfig();
        expConfig.setExperimentAssignments(assignments);
        ctx.setExpConfig(expConfig);
    }

    /**
     * 执行具体的分流逻辑
     */
    private String shuntUser(String userId, ExperimentDef exp) {
        // 使用 userId + experimentId 作为哈希种子，确保实验之间不耦合
        String seed = userId + "_" + exp.id();
        int bucket = Math.abs(seed.hashCode()) % TOTAL_BUCKETS;

        if (exp.groups() == null) {
            return "default";
        }

        // 查找落在范围内的分组
        for (ExperimentDef.Variant group : exp.groups()) {
            if (group.isInRange(bucket)) {
                return group.name();
            }
        }

        return "default"; // 如果没命中任何配置范围，走默认组
    }
}
