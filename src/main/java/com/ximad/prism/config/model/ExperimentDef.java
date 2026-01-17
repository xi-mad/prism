package com.ximad.prism.config.model;

import java.util.List;

/**
 * 实验定义 - 支持 1000 桶精细化分流
 */
public record ExperimentDef(
    String id,          // 实验唯一标识
    List<Variant> groups // 实验分组定义
) {
    /**
     * 分组定义
     * @param name 分组名称 (如 "control", "test_v1")
     * @param range 占据的桶范围 (如 "0-499" 表示 50% 流量)
     */
    public record Variant(
        String name,
        String range
    ) {
        /**
         * 检查指定的桶号是否落在该分组范围内
         */
        public boolean isInRange(int bucket) {
            if (range == null || !range.contains("-")) return false;
            String[] parts = range.split("-");
            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());
            return bucket >= start && bucket <= end;
        }
    }
}
