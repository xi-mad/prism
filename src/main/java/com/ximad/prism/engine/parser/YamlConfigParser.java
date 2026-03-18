package com.ximad.prism.engine.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ximad.prism.config.model.ScenarioConfig;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class YamlConfigParser implements ConfigParser {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public boolean supports(String configContent) {
        // Fallback for non-JSON content, assuming it is YAML
        return configContent != null && !configContent.trim().startsWith("{");
    }

    @Override
    public ScenarioConfig parse(String configContent) throws Exception {
        return yamlMapper.readValue(configContent, ScenarioConfig.class);
    }
}
