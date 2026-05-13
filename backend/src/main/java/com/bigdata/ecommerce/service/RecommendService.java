package com.bigdata.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.entity.UserBehaviorLog;
import com.bigdata.ecommerce.mapper.ProductMapper;
import com.bigdata.ecommerce.mapper.UserBehaviorLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    @Autowired
    private ProductMapper productMapper;

    public List<Product> getPersonalRecommend(Long userId, int limit) {
        if (userId == null) {
            return getRandomProducts(limit);
        }

        List<Product> result = new ArrayList<>();

        List<Map<String, Object>> categoryStats = behaviorLogMapper.getUserCategoryStats(userId, 3);
        if (!categoryStats.isEmpty()) {
            List<String> topCategories = categoryStats.stream()
                    .map(m -> (String) m.get("category"))
                    .collect(Collectors.toList());

            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Product::getCategory, topCategories);
            wrapper.orderByDesc(Product::getCreateTime);
            wrapper.last("LIMIT " + limit);
            result = productMapper.selectList(wrapper);
        }

        if (result.size() < limit) {
            List<Long> existIds = result.stream().map(Product::getId).collect(Collectors.toList());
            List<Product> collabProducts = getCollaborativeProducts(userId, limit - result.size(), existIds);
            result.addAll(collabProducts);
        }

        if (result.size() < limit) {
            List<Long> existIds = result.stream().map(Product::getId).collect(Collectors.toList());
            List<Product> randomFill = getRandomExclude(limit - result.size(), existIds);
            result.addAll(randomFill);
        }

        return result.stream().limit(limit).collect(Collectors.toList());
    }

    private List<Product> getCollaborativeProducts(Long userId, int limit, List<Long> excludeIds) {
        try {
            List<Long> collabIds = behaviorLogMapper.getCollaborativeProducts(userId, limit);
            if (collabIds.isEmpty()) {
                return new ArrayList<>();
            }
            collabIds = collabIds.stream().filter(id -> !excludeIds.contains(id)).collect(Collectors.toList());
            if (collabIds.isEmpty()) {
                return new ArrayList<>();
            }
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(Product::getId, collabIds);
            wrapper.last("LIMIT " + limit);
            return productMapper.selectList(wrapper);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Product> getRandomProducts(int limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreateTime);
        wrapper.last("LIMIT " + limit);
        return productMapper.selectList(wrapper);
    }

    private List<Product> getRandomExclude(int limit, List<Long> excludeIds) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (!excludeIds.isEmpty()) {
            wrapper.notIn(Product::getId, excludeIds);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        wrapper.last("LIMIT " + limit);
        return productMapper.selectList(wrapper);
    }

    public Map<String, Object> getUserProfile(Long userId) {
        Map<String, Object> profile = new HashMap<>();
        if (userId == null) {
            profile.put("categoryStats", Collections.emptyList());
            profile.put("totalBehaviors", 0);
            return profile;
        }

        List<Map<String, Object>> categoryStats = behaviorLogMapper.getUserCategoryStats(userId, 10);
        profile.put("categoryStats", categoryStats);

        LambdaQueryWrapper<UserBehaviorLog> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(UserBehaviorLog::getUserId, userId);
        Long total = behaviorLogMapper.selectCount(countWrapper);
        profile.put("totalBehaviors", total);

        return profile;
    }
}
