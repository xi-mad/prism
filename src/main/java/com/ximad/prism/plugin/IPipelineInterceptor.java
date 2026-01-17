package com.ximad.prism.plugin;

import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.engine.PipelineStage;

/**
 * 流水线拦截器接口
 */
public interface IPipelineInterceptor {
    /**
     * 阶段执行前
     */
    default void preExecute(PipelineStage stage, RecContext ctx) {}

    /**
     * 阶段执行后
     */
    default void postExecute(PipelineStage stage, RecContext ctx) {}
}
