package com.bigdata.ecommerce.service;

import com.alibaba.fastjson.JSON;
import com.bigdata.ecommerce.entity.UserBehavior;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.conf.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnalyticsService {

    @Autowired
    private Configuration hbaseConfiguration;

    @KafkaListener(topics = "user-behavior", groupId = "ecommerce-analytics")
    public void consumeBehavior(String message) {
        try {
            UserBehavior behavior = JSON.parseObject(message, UserBehavior.class);
            saveToHBase(behavior);
            log.info("Consumed behavior: userId={}, event={}", behavior.getUserId(), behavior.getEventType());
        } catch (Exception e) {
            log.error("Failed to process behavior: {}", e.getMessage());
        }
    }

    private void saveToHBase(UserBehavior behavior) {
        try (Connection connection = ConnectionFactory.createConnection(hbaseConfiguration)) {
            Table table = connection.getTable(TableName.valueOf("user_behavior"));
            String rowKey = behavior.getUserId() + "_" + behavior.getTimestamp();
            Put put = new Put(rowKey.getBytes());
            put.addColumn("info".getBytes(), "userId".getBytes(), String.valueOf(behavior.getUserId()).getBytes());
            put.addColumn("info".getBytes(), "eventType".getBytes(), behavior.getEventType().getBytes());
            if (behavior.getProductId() != null) {
                put.addColumn("info".getBytes(), "productId".getBytes(), String.valueOf(behavior.getProductId()).getBytes());
            }
            if (behavior.getCategory() != null) {
                put.addColumn("info".getBytes(), "category".getBytes(), behavior.getCategory().getBytes());
            }
            if (behavior.getKeyword() != null) {
                put.addColumn("info".getBytes(), "keyword".getBytes(), behavior.getKeyword().getBytes());
            }
            put.addColumn("info".getBytes(), "timestamp".getBytes(), String.valueOf(behavior.getTimestamp()).getBytes());
            table.put(put);
        } catch (Exception e) {
            log.error("Failed to save to HBase: {}", e.getMessage());
        }
    }
}
