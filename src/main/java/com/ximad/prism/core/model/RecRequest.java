package com.ximad.prism.core.model;

import java.util.Map;

public record RecRequest(
        String userId,
        String cityId,
        String scenarioId,
        Map<String, String> deviceMeta,
        int limit,
        boolean debugMode
) {
}
