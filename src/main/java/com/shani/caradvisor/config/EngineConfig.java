package com.shani.caradvisor.config;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.engine.ComparisonEngine;
import com.shani.caradvisor.engine.Rules;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/** Wires the framework-free engine into Spring. The engine itself knows nothing about Spring. */
@Configuration
public class EngineConfig {

    @Bean
    ReferenceData referenceData(@Value("${advisor.data-dir:data}") String dataDir) {
        return ReferenceData.load(Path.of(dataDir));
    }

    @Bean
    Rules rules(ReferenceData referenceData) {
        return referenceData.rules();
    }

    @Bean
    ComparisonEngine comparisonEngine(Rules rules) {
        return new ComparisonEngine(rules);
    }
}
