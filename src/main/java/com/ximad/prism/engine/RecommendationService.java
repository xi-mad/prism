package com.ximad.prism.engine;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ximad.prism.config.model.ScenarioConfig;
import com.ximad.prism.core.context.RecContext;
import com.ximad.prism.core.context.RecScope;
import com.ximad.prism.core.model.RecRequest;
import com.ximad.prism.core.model.RecommendItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    // Define Scope (ThreadLocal alternative)
    // Note: Constants usually static, but ScopedValue is fine as static final
    // Using RecScope.CTX defined in RecScope class

    private final PipelineExecutor pipelineExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<RecommendItem> recommend(RecRequest request, String yamlConfig) {
        try {
            // 1. Parse Config
            ScenarioConfig config = yamlMapper.readValue(yamlConfig, ScenarioConfig.class);

            // 2. Initialize Context
            RecContext context = new RecContext(request);
            context.setConfig(config); // Set Parsed Config

            // 3. Bind Scope and Execute
            return ScopedValue.where(RecScope.CTX, context).call(() -> {
                try {
                    List<RecommendItem> result = pipelineExecutor.execute();

                    if (request.debugMode()) {
                        log.info("\n--- PROCESS TRACE (SUMMARY) ---\n{}\n", context.getTrace().printSummary());
                        log.info("\n--- FULL STRUCTURED TRACE (JSON) ---\n{}\n",
                                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context.getTrace()));
                    }
                    return result;
                } catch (Exception e) {
                    log.error("Pipeline Execution Error. Summary: {}", context.getTrace().printSummary(), e);
                    return Collections.emptyList();
                }
            });
        } catch (Exception e) {
            log.error("[Trace] ", e);
            return Collections.emptyList();
        }
    }
}
