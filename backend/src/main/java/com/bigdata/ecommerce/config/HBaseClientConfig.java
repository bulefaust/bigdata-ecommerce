package com.bigdata.ecommerce.config;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HBaseClientConfig {

    @Autowired
    private HBaseConfig hBaseConfig;

    @Bean
    public org.apache.hadoop.conf.Configuration hbaseConfiguration() {
        org.apache.hadoop.conf.Configuration config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", hBaseConfig.getQuorum());
        config.set("hbase.zookeeper.property.clientPort", String.valueOf(hBaseConfig.getPort()));
        return config;
    }
}
