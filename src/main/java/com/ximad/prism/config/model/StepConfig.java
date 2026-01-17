package com.ximad.prism.config.model;

import java.util.Map;

public record StepConfig(
    String name,
    double weight, // Only for recall
    Map<String, Object> params
) {}
