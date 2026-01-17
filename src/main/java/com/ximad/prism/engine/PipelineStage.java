package com.ximad.prism.engine;

/**
 * 推荐流水线阶段定义
 */
public enum PipelineStage {
    EXPERIMENT,
    PROFILE,
    RECALL,
    FILTER,
    RANK,
    RERANK,
    HYDRATION
}
