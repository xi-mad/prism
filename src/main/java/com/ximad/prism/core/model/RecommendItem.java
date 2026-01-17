package com.ximad.prism.core.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public final class RecommendItem {
    private final String id;
    private final String bizType; // String identifier matching config
    private double score;
    private final Map<String, Object> features = new HashMap<>();
    private final Map<String, Object> displayData = new HashMap<>();
    private final List<Reason> reasons = new ArrayList<>();

    public RecommendItem(String id, String bizType) {
        this.id = id;
        this.bizType = bizType;
    }

    public void addReason(String type, String text) {
        this.reasons.add(new Reason(type, text));
    }
}
