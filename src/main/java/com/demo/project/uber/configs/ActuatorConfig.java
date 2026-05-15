package com.demo.project.uber.configs;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
public class ActuatorConfig {

    @Bean
    public InfoContributor infoContributor() {
        return (Info.Builder builder) -> builder.withDetails(Map.of(
                "app", Map.of(
                        "name", "Uber Backend",
                        "version", "1.0.0",
                        "description", "Uber-like ride hailing backend"
                ),
                "build", Map.of(
                        "timestamp", LocalDateTime.now().toString()
                )
        ));
    }
}
