package com.ximad.prism.config.model;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
public record ScenarioConfig(
    String scenarioId,
    Map<String, Object> params,
    
    // Experiment definitions
    List<ExperimentDef> experiments,
    
    // All stages use unified ConditionalStageConfig
    // If 'when' is null/missing, it's the default (no branching)
    List<ConditionalStageConfig> experiment,
    List<ConditionalStageConfig> profile,
    List<ConditionalStageConfig> filters,
    List<ConditionalStageConfig> recall,
    List<ConditionalStageConfig> ranking,
    List<ConditionalStageConfig> rerank,
    List<ConditionalStageConfig> hydration,
    List<String> interceptors,
    Map<String, Integer> timeouts
) {}
