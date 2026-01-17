package com.ximad.prism.plugin;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.model.RecommendItem;

import com.ximad.prism.core.context.RecContext;

public interface IFilterStrategy {
    // Returns true to keep, false to filter out
    FilterResult filter(RecContext ctx, RecommendItem item, StepConfig config);
}
