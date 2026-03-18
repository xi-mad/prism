package com.ximad.prism.engine;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ximad.prism.config.model.ScenarioConfig;
import com.ximad.prism.core.context.ProcessTrace;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.context.RecScope;
import com.ximad.prism.core.model.RecRequest;
import com.ximad.prism.core.model.RecommendItem;
import com.ximad.prism.engine.parser.ConfigParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PipelineExecutor pipelineExecutor;
    private final List<ConfigParser> configParsers;

    public List<RecommendItem> recommend(RecRequest request, String configContent) {
        return recommendWithTrace(request, configContent).items();
    }
    
    public RecommendResult recommendWithTrace(RecRequest request, String configContent) {
        try {
            ScenarioConfig config = null;
            for (ConfigParser parser : configParsers) {
                if (parser.supports(configContent)) {
                    config = parser.parse(configContent);
                    break;
                }
            }
            
            if (config == null) {
                throw new IllegalArgumentException("No suitable parser found for configuration content");
            }
            
            RecContext context = new RecContext(request);
            context.setConfig(config);

            List<RecommendItem> items = ScopedValue.where(RecScope.CTX, context).call(() -> {
                try {
                    List<RecommendItem> result = pipelineExecutor.execute();
                    if (request.debugMode()) {
                        log.info("\n--- PROCESS TRACE (SUMMARY) ---\n{}\n", context.getTrace().printSummary());
                    }
                    return result;
                } catch (Exception e) {
                    log.error("Pipeline Execution Error. Summary: {}", context.getTrace().printSummary(), e);
                    return Collections.emptyList();
                }
            });
            
            return new RecommendResult(items, context.getTrace());
        } catch (Exception e) {
            log.error("[Trace] Config parsing or execution failed", e);
            return new RecommendResult(Collections.emptyList(), null);
        }
    }
    
    public record RecommendResult(List<RecommendItem> items, ProcessTrace trace) {}
}

