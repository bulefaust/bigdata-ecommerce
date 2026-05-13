package com.bigdata.ecommerce.controller;

import com.bigdata.ecommerce.entity.UserBehavior;
import com.bigdata.ecommerce.entity.UserBehaviorLog;
import com.bigdata.ecommerce.mapper.UserBehaviorLogMapper;
import com.bigdata.ecommerce.tracker.BehaviorTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/track")
public class TrackController {

    @Autowired
    private BehaviorTracker behaviorTracker;

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    @PostMapping("/behavior")
    public String trackBehavior(@RequestBody UserBehavior behavior) {
        behaviorTracker.track(behavior);

        UserBehaviorLog log = new UserBehaviorLog();
        log.setUserId(behavior.getUserId());
        log.setEventType(behavior.getEventType());
        log.setProductId(behavior.getProductId());
        log.setCategory(behavior.getCategory());
        log.setKeyword(behavior.getKeyword());
        log.setDuration(behavior.getDuration());
        log.setTimestamp(behavior.getTimestamp());
        try {
            behaviorLogMapper.insert(log);
        } catch (Exception e) {
        }

        return "ok";
    }
}
