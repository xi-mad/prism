package com.ximad.prism.controller;

import com.ximad.prism.core.context.ProcessTrace;
import com.ximad.prism.core.model.RecRequest;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.RecommendationService;
import com.ximad.prism.plugin.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RecommendController {

    private final RecommendationService recommendationService;
    
    // 注入所有策略 Map
    private final Map<String, IRecallStrategy> recallStrategies;
    private final Map<String, IFilterStrategy> filterStrategies;
    private final Map<String, IRankStrategy> rankStrategies;
    private final Map<String, IRerankStrategy> rerankStrategies;
    private final Map<String, IHydrationStrategy> hydrationStrategies;
    private final Map<String, IProfileStrategy> profileStrategies;
    private final Map<String, IExperimentStrategy> experimentStrategies;

    /**
     * 使用自定义配置获取推荐
     */
    @PostMapping("/recommend")
    public Map<String, Object> recommendWithConfig(@RequestBody RecommendRequest req) {
        log.info("API POST Request: userId={}", req.userId());
        
        String config = req.config();
        RecRequest request = new RecRequest(req.userId(), "1", req.scenario(), new HashMap<>(), 0, false);
        RecommendationService.RecommendResult result = recommendationService.recommendWithTrace(request, config);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", req.userId());
        response.put("scenario", req.scenario());
        response.put("total", result.items().size());
        response.put("items", result.items());
        
        if (req.debug()) {
            response.put("trace", result.trace());
        }
        
        return response;
    }

    /**
     * 获取所有可用策略 (Debug 模式)
     */
    @GetMapping("/strategies")
    public Map<String, Object> getStrategies() {
        Map<String, Object> strategies = new HashMap<>();
        strategies.put("recall", recallStrategies.keySet());
        strategies.put("filter", filterStrategies.keySet());
        strategies.put("rank", rankStrategies.keySet());
        strategies.put("rerank", rerankStrategies.keySet());
        strategies.put("hydration", hydrationStrategies.keySet());
        strategies.put("profile", profileStrategies.keySet());
        strategies.put("experiment", experimentStrategies.keySet());
        return strategies;
    }

    public record RecommendRequest(
        String userId,
        String scenario,
        boolean debug,
        String config
    ) {}
}
