package com.ximad.prism.config.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Unified conditional stage configuration.
 * Supports all stages including Recall, Filter, Rerank, Hydration, Profile, Experiment, and Rank.
 * If 'when' is null, this is the default/fallback config (matches all).
 * 
 * @param when Optional condition to match (null = default/fallback)
 * @param steps List of steps for step-based stages (Recall, Filter, Rerank, etc.)
 * @param modelId Model ID for Rank stage
 * @param fusionFormula Fusion formula for Rank stage
 * @param calibration Calibration map for Rank stage
 */
public record ConditionalStageConfig(
    WhenCondition when,
    List<StepConfig> steps,
    String modelId,
    String fusionFormula,
    Map<String, Double> calibration
) {
    /**
     * Check if this config matches the given experiment assignments.
     */
    public boolean matches(Map<String, String> assignments) {
        return when == null || when.matches(assignments);
    }
    
    /**
     * Utility method to find matching steps from a list of conditional configs.
     */
    public static List<StepConfig> findMatchingSteps(List<ConditionalStageConfig> configs, Map<String, String> assignments) {
        if (configs == null) return Collections.emptyList();
        List<StepConfig> allSteps = new ArrayList<>();
        for (ConditionalStageConfig cfg : configs) {
            if (cfg.matches(assignments)) {
                if (cfg.steps() != null) {
                    allSteps.addAll(cfg.steps());
                }
            }
        }
        return allSteps;
    }
    
    /**
     * Utility method to find matching rank config from a list of conditional configs.
     */
    public static ConditionalStageConfig findMatchingRankConfig(List<ConditionalStageConfig> configs, Map<String, String> assignments) {
        if (configs == null) return null;
        for (ConditionalStageConfig cfg : configs) {
            if (cfg.matches(assignments)) {
                return cfg;
            }
        }
        return null;
    }
}
