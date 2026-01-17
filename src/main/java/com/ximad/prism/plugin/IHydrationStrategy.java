package com.ximad.prism.plugin;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.model.RecommendItem;
import java.util.List;

public interface IHydrationStrategy {
    List<RecommendItem> hydrate(RecContext ctx, List<RecommendItem> items, StepConfig config);
}
