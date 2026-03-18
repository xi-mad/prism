package com.ximad.prism.engine.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ximad.prism.config.model.ScenarioConfig;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class JsonConfigParser implements ConfigParser {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public boolean supports(String configContent) {
        return configContent != null && configContent.trim().startsWith("{");
    }

    @Override
    public ScenarioConfig parse(String configContent) throws Exception {
        return objectMapper.readValue(configContent, ScenarioConfig.class);
    }
}
