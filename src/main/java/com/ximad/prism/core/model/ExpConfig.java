package com.ximad.prism.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpConfig {
    private String rankModelId;
    private boolean enableExploration;
    private Map<String, Double> bizWeights = new HashMap<>(); // Business weight adjustments

    // Experiment assignments: experimentId -> variant
    private Map<String, String> experimentAssignments = new HashMap<>();

    public String getVariant(String experimentId) {
        return experimentAssignments.getOrDefault(experimentId, "default");
    }

    public void assignVariant(String experimentId, String variant) {
        experimentAssignments.put(experimentId, variant);
    }
}
