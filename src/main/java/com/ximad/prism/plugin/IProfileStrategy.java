package com.ximad.prism.plugin;

import com.ximad.prism.config.model.StepConfig;
import com.ximad.prism.core.context.RecContext;

public interface IProfileStrategy {
    void load(RecContext ctx, StepConfig config);
}
