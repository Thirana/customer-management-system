package com.example.customermanagement.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .modules(new JavaTimeModule())
                .featuresToDisable(
                        // API dates should remain readable ISO strings, not numeric timestamps.
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        // Extra client fields should not break requests unless validation rejects them.
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                );
    }
}
