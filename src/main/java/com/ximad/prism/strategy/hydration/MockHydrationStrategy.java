package com.ximad.prism.strategy.hydration;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.plugin.IHydrationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

@Component("MockHydration")
@Slf4j
public class MockHydrationStrategy implements IHydrationStrategy {
    @Override
    public List<RecommendItem> hydrate(RecContext ctx, List<RecommendItem> items, StepConfig config) {
        // Mock Detail Parallel Fetch
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {
             // ... simplify for verification
             // In real logic: group by bizType, fetch details
        }
        
        // Async Exposure Log
        List<String> ids = items.stream().map(RecommendItem::getId).toList();
        CompletableFuture.runAsync(() -> {
            log.info("Async Logging Exposure: {}", ids);
            // Mock DB write
        });
        
        return items;
    }
}
