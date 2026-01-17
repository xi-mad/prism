package com.ximad.prism.config.model;

import java.util.Map;

public record RankConfig(
    String modelId,
    String fusionFormula,
    Map<String, Double> calibration
) {}
