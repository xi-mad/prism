package com.ximad.prism.config.model;

import java.util.Map;

/**
 * Condition for matching experiment variant.
 * @param experiment Experiment ID to check
 * @param variant Expected variant value
 */
public record WhenCondition(
    String experiment,
    String variant
) {
    public boolean matches(Map<String, String> assignments) {
        return variant.equals(assignments.get(experiment));
    }
}
