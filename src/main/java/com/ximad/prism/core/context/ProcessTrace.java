package com.ximad.prism.core.context;

import com.ximad.prism.engine.PipelineStage;
import lombok.*;
import org.slf4j.helpers.MessageFormatter;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 轻量化执行追踪器 - 支持 [阶段->插件->日志] 的三级追踪架构
 */
@Getter
public class ProcessTrace {

    // 阶段明细 [Stage -> Metrics]
    private final Map<PipelineStage, StageMetrics> stages = Collections.synchronizedMap(new LinkedHashMap<>());

    // 全局日志记录
    private final List<String> globalLogs = Collections.synchronizedList(new ArrayList<>());

    public void startStage(PipelineStage stage) {
        stages.put(stage, new StageMetrics(stage));
    }

    public void endStage(PipelineStage stage, List<String> inputIds, List<String> outputIds) {
        StageMetrics metrics = stages.get(stage);
        if (metrics != null) {
            metrics.complete(inputIds, outputIds);
        }
    }

    /**
     * 记录插件的核心指标（耗时、数量）
     */
    public void recordPlugin(PipelineStage stage, String pluginName, long latencyMs, int currentSize) {
        StageMetrics stageMetrics = stages.get(stage);
        if (stageMetrics != null) {
            PluginMetrics pm = stageMetrics.getOrCreatePlugin(pluginName);
            pm.setLatency(latencyMs);
            pm.setCurrentSize(currentSize);
        }
    }

    /**
     * 记录属于指定阶段的通用日志
     */
    public void log(PipelineStage stage, String format, Object... args) {
        StageMetrics metrics = stages.get(stage);
        if (metrics == null) return;

        // 自动魔法：检查当前线程是否处于某个插件的作用域内
        if (PluginContext.CURRENT_PLUGIN_NAME.isBound()) {
            String pluginName = PluginContext.CURRENT_PLUGIN_NAME.get();
            PluginMetrics pm = metrics.getOrCreatePlugin(pluginName);
            pm.addLog(MessageFormatter.arrayFormat(format, args).getMessage());
        } else {
            // 不在插件内，记录为阶段通用日志
            metrics.addLog(MessageFormatter.arrayFormat(format, args).getMessage());
        }
    }

    /**
     * 记录全局日志
     */
    public void log(String format, Object... args) {
        LocalDateTime now = LocalDateTime.now();
        globalLogs.add(String.format("[%tF %tT] %s", now, now,
                MessageFormatter.arrayFormat(format, args).getMessage()));
    }

    public String printSummary() {
        StringBuilder sb = new StringBuilder("Pipeline Funnel: ");
        stages.forEach((s, m) -> sb.append(String.format("[%s: %dms, %d->%d] ",
                s, m.getDuration(), m.getInputIds().size(), m.getOutputIds().size())));
        return sb.toString();
    }

    @Data
    @RequiredArgsConstructor
    public static class StageMetrics {
        private final PipelineStage stage;
        private LocalDateTime now = LocalDateTime.now();
        private long startTime = System.currentTimeMillis();
        private long duration;

        private List<String> inputIds = new ArrayList<>();
        private List<String> outputIds = new ArrayList<>();

        // 插件指标：使用有序 Map 方便按插件名查找和记录日志
        private final Map<String, PluginMetrics> plugins = Collections.synchronizedMap(new LinkedHashMap<>());
        
        private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

        public PluginMetrics getOrCreatePlugin(String name) {
            return plugins.computeIfAbsent(name, PluginMetrics::new);
        }

        public void complete(List<String> in, List<String> out) {
            this.duration = System.currentTimeMillis() - startTime;
            this.inputIds = in != null ? new ArrayList<>(in) : Collections.emptyList();
            this.outputIds = out != null ? new ArrayList<>(out) : Collections.emptyList();
        }

        public void addLog(String msg) {
            LocalDateTime now = LocalDateTime.now();
            logs.add(String.format("[%tF %tT] %s", now, now, msg));
        }
    }

    @Data
    @NoArgsConstructor
    public static class PluginMetrics {
        private String name;
        private long latency;
        private int currentSize;
        private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

        public PluginMetrics(String name) {
            this.name = name;
        }

        public void addLog(String msg) {
            LocalDateTime now = LocalDateTime.now();
            logs.add(String.format("[%tF %tT] %s", now, now, msg));
        }
    }
}
