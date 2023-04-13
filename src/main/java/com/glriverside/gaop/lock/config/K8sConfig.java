package com.glriverside.gaop.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "k8s.api")
@Data
public class K8sConfig {
    private boolean enabled;
    private boolean inCluster;
    private String configFile;
    private boolean logEnabled;
    private String logLevel;
}
