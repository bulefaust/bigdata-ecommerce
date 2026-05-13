package com.bigdata.ecommerce.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.entity.UserFavorite;
import com.bigdata.ecommerce.mapper.ProductMapper;
import com.bigdata.ecommerce.mapper.UserFavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private UserFavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @PostMapping("/toggle")
    public Map<String, Object> toggle(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());

        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getProductId, productId);
        UserFavorite existing = favoriteMapper.selectOne(wrapper);

        Map<String, Object> result = new HashMap<>();
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            result.put("favorited", false);
        } else {
            UserFavorite fav = new UserFavorite();
            fav.setUserId(userId);
            fav.setProductId(productId);
            favoriteMapper.insert(fav);
            result.put("favorited", true);
        }
        return result;
    }

    @GetMapping("/check")
    public Map<String, Object> check(@RequestParam Long userId, @RequestParam Long productId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getProductId, productId);
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favoriteMapper.selectCount(wrapper) > 0);
        return result;
    }

    @GetMapping("/list")
    public List<Map<String, Object>> list(@RequestParam Long userId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .orderByDesc(UserFavorite::getCreateTime);
        List<UserFavorite> favorites = favoriteMapper.selectList(wrapper);

        if (favorites.isEmpty()) return Collections.emptyList();

        List<Long> productIds = favorites.stream()
                .map(UserFavorite::getProductId)
                .collect(Collectors.toList());
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = new HashMap<>();
        for (Product p : products) {
            productMap.put(p.getId(), p);
        }

        return favorites.stream().map(fav -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", fav.getId());
            map.put("productId", fav.getProductId());
            map.put("createTime", fav.getCreateTime());
            Product p = productMap.get(fav.getProductId());
            if (p != null) {
                map.put("name", p.getName());
                map.put("price", p.getPrice());
                map.put("imageUrl", p.getImageUrl());
                map.put("category", p.getCategory());
                map.put("stock", p.getStock());
            }
            return map;
        }).collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        favoriteMapper.deleteById(id);
    }
}
