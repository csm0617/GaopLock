package com.glriverside.gaop.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "batch")
@Data
public class OrdersConfig {
    private String order;
}
