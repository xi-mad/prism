package com.ximad.prism.engine.parser;

import com.ximad.prism.config.model.ScenarioConfig;

public interface ConfigParser {
    boolean supports(String configContent);
    ScenarioConfig parse(String configContent) throws Exception;
}
