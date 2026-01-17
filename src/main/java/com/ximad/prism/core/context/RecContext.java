package com.ximad.prism.core.context;

import com.ximad.prism.config.model.ScenarioConfig;
import com.ximad.prism.core.model.ExpConfig;
import com.ximad.prism.core.model.RecRequest;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.core.model.UserProfile;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context request holder, designed to be used with ScopedValue.
 */
@Data
public class RecContext {
    private final RecRequest request;
    private final ProcessTrace trace;
    private final long startTime;
    
    // 1. Experiment Config
    private ExpConfig expConfig;
    
    // 2. User Profile
    private UserProfile userProfile;
    
    // 3. Exposure History (IDs already shown to user)
    private List<String> exposureHistory = new ArrayList<>();
    
    // 3. Performance Log
    private final Map<String, Long> performanceLog = new ConcurrentHashMap<>();

    // Runtime configuration (Legacy but kept for compatibility during refactor)
    private ScenarioConfig config; 

    // Candidates list
    private volatile List<RecommendItem> candidates = new ArrayList<>();

    public RecContext(RecRequest request) {
        this.request = request;
        this.trace = new ProcessTrace();
        this.startTime = System.currentTimeMillis();
    }
}
