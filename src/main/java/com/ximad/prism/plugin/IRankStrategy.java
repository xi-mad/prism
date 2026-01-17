package com.ximad.prism.plugin;

import com.ximad.prism.config.model.RankConfig;
import com.ximad.prism.core.context.RecContext;

public interface IRankStrategy {
    void rank(RecContext ctx, RankConfig config);
}
