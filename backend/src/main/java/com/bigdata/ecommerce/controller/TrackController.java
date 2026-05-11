package com.bigdata.ecommerce.controller;

import com.bigdata.ecommerce.entity.UserBehavior;
import com.bigdata.ecommerce.tracker.BehaviorTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/track")
public class TrackController {

    @Autowired
    private BehaviorTracker behaviorTracker;

    @PostMapping("/behavior")
    public String trackBehavior(@RequestBody UserBehavior behavior) {
        behaviorTracker.track(behavior);
        return "ok";
    }
}
