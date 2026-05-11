package com.bigdata.ecommerce.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserBehavior implements Serializable {
    private Long userId;
    private String eventType;
    private Long productId;
    private String category;
    private String keyword;
    private Integer duration;
    private Long timestamp;
    private String sessionId;
    private String ip;
    private String userAgent;

    public static UserBehavior click(Long userId, Long productId, String category) {
        UserBehavior b = new UserBehavior();
        b.setUserId(userId);
        b.setEventType("CLICK");
        b.setProductId(productId);
        b.setCategory(category);
        b.setTimestamp(System.currentTimeMillis());
        return b;
    }

    public static UserBehavior browse(Long userId, Long productId, String category, Integer duration) {
        UserBehavior b = new UserBehavior();
        b.setUserId(userId);
        b.setEventType("BROWSE");
        b.setProductId(productId);
        b.setCategory(category);
        b.setDuration(duration);
        b.setTimestamp(System.currentTimeMillis());
        return b;
    }

    public static UserBehavior search(Long userId, String keyword) {
        UserBehavior b = new UserBehavior();
        b.setUserId(userId);
        b.setEventType("SEARCH");
        b.setKeyword(keyword);
        b.setTimestamp(System.currentTimeMillis());
        return b;
    }

    public static UserBehavior purchase(Long userId, Long productId, String category) {
        UserBehavior b = new UserBehavior();
        b.setUserId(userId);
        b.setEventType("PURCHASE");
        b.setProductId(productId);
        b.setCategory(category);
        b.setTimestamp(System.currentTimeMillis());
        return b;
    }

    public static UserBehavior cart(Long userId, Long productId, String category) {
        UserBehavior b = new UserBehavior();
        b.setUserId(userId);
        b.setEventType("CART");
        b.setProductId(productId);
        b.setCategory(category);
        b.setTimestamp(System.currentTimeMillis());
        return b;
    }
}
