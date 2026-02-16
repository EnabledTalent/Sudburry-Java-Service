package com.et.SudburyCityPlatform.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConfigClass {
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
