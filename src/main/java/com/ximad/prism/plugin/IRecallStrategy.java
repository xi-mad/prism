package com.ximad.prism.plugin;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.model.RecommendItem;

import com.ximad.prism.core.context.RecContext;

import java.util.List;

public interface IRecallStrategy {
    List<RecommendItem> recall(RecContext ctx, StepConfig config);
}
