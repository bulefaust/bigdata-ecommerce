package com.bigdata.ecommerce.tracker;

import com.alibaba.fastjson.JSON;
import com.bigdata.ecommerce.entity.UserBehavior;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BehaviorTracker {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "user-behavior";

    public void track(UserBehavior behavior) {
        try {
            String json = JSON.toJSONString(behavior);
            kafkaTemplate.send(TOPIC, String.valueOf(behavior.getUserId()), json);
            log.debug("Tracked behavior: {}", json);
        } catch (Exception e) {
            log.error("Failed to track behavior: {}", e.getMessage());
        }
    }

    public void trackClick(Long userId, Long productId, String category) {
        track(UserBehavior.click(userId, productId, category));
    }

    public void trackBrowse(Long userId, Long productId, String category, Integer duration) {
        track(UserBehavior.browse(userId, productId, category, duration));
    }

    public void trackSearch(Long userId, String keyword) {
        track(UserBehavior.search(userId, keyword));
    }

    public void trackPurchase(Long userId, Long productId, String category) {
        track(UserBehavior.purchase(userId, productId, category));
    }

    public void trackCart(Long userId, Long productId, String category) {
        track(UserBehavior.cart(userId, productId, category));
    }
}
