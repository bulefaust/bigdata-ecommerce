package com.bigdata.ecommerce.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.entity.ProductSpec;
import com.bigdata.ecommerce.entity.ProductReview;
import com.bigdata.ecommerce.entity.User;
import com.bigdata.ecommerce.mapper.ProductMapper;
import com.bigdata.ecommerce.mapper.ProductSpecMapper;
import com.bigdata.ecommerce.mapper.ProductReviewMapper;
import com.bigdata.ecommerce.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
public class ProductDetailController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductSpecMapper specMapper;

    @Autowired
    private ProductReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/{id}/specs")
    public List<ProductSpec> getSpecs(@PathVariable Long id) {
        LambdaQueryWrapper<ProductSpec> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSpec::getProductId, id);
        return specMapper.selectList(wrapper);
    }

    @GetMapping("/{id}/reviews")
    public List<Map<String, Object>> getReviews(@PathVariable Long id) {
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, id);
        wrapper.orderByDesc(ProductReview::getCreateTime);
        List<ProductReview> reviews = reviewMapper.selectList(wrapper);

        List<Long> userIds = reviews.stream()
                .map(ProductReview::getUserId)
                .filter(uid -> uid != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User u : users) {
                userMap.put(u.getId(), u);
            }
        }

        return reviews.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("productId", r.getProductId());
            map.put("userId", r.getUserId());
            map.put("username", r.getUsername());
            map.put("rating", r.getRating());
            map.put("content", r.getContent());
            map.put("createTime", r.getCreateTime());
            if (r.getUserId() != null && userMap.containsKey(r.getUserId())) {
                User u = userMap.get(r.getUserId());
                map.put("role", u.getRole());
                if ("seller".equals(u.getRole()) && u.getStoreName() != null) {
                    map.put("storeName", u.getStoreName());
                }
            } else {
                map.put("role", "user");
            }
            return map;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/reviews")
    public Map<String, Object> submitReview(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> err = new HashMap<>();
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            Integer rating = Integer.valueOf(body.get("rating").toString());
            String content = body.get("content").toString();

            User user = userMapper.selectById(userId);
            if (user == null) {
                err.put("error", "用户不存在");
                return err;
            }

            Product product = productMapper.selectById(id);
            if (product != null && product.getSellerId() != null && product.getSellerId().equals(userId)) {
                err.put("error", "商家不能评价自己的商品");
                return err;
            }

            ProductReview review = new ProductReview();
            review.setProductId(id);
            review.setUserId(userId);
            review.setUsername(user.getUsername());
            review.setRating(rating);
            review.setContent(content);
            review.setCreateTime(java.time.LocalDateTime.now().toString());
            reviewMapper.insert(review);

            Map<String, Object> result = new HashMap<>();
            result.put("id", review.getId());
            result.put("productId", review.getProductId());
            result.put("userId", review.getUserId());
            result.put("username", review.getUsername());
            result.put("rating", review.getRating());
            result.put("content", review.getContent());
            result.put("createTime", review.getCreateTime());
            result.put("role", user.getRole());
            if ("seller".equals(user.getRole()) && user.getStoreName() != null) {
                result.put("storeName", user.getStoreName());
            }
            return result;
        } catch (Exception e) {
            err.put("error", e.getMessage() != null ? e.getMessage() : e.getClass().getName());
            err.put("detail", e.toString());
            return err;
        }
    }
}
