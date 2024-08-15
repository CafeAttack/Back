package com.cafeattack.springboot.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-map.properties")
public class MapConfig {
    @Value("${rest.api.key}")
    private String apiKey;

    @Bean
    public String getApiKey() {
        return this.apiKey;
    }
}
