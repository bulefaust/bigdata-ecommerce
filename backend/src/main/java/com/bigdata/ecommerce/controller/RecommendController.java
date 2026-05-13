package com.bigdata.ecommerce.controller;

import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @GetMapping("/personal")
    public List<Product> personalRecommend(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "8") int limit) {
        return recommendService.getPersonalRecommend(userId, limit);
    }

    @GetMapping("/profile")
    public Map<String, Object> userProfile(@RequestParam Long userId) {
        return recommendService.getUserProfile(userId);
    }
}
