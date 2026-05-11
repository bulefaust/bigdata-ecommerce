package com.bigdata.ecommerce.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "hbase")
public class HBaseConfig {
    private String quorum;
    private int port;
}
