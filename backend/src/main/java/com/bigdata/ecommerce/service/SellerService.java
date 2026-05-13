package com.bigdata.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bigdata.ecommerce.entity.Order;
import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.entity.UserBehaviorLog;
import com.bigdata.ecommerce.mapper.OrderMapper;
import com.bigdata.ecommerce.mapper.ProductMapper;
import com.bigdata.ecommerce.mapper.UserBehaviorLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SellerService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    public List<Product> getMyProducts(Long sellerId) {
        return productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, sellerId)
                .orderByDesc(Product::getCreateTime)
        );
    }

    public List<Product> getAllProducts() {
        return productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreateTime)
        );
    }

    public List<Product> getPendingProducts() {
        return productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 2)
                .orderByDesc(Product::getCreateTime)
        );
    }

    public Product addProduct(Product product) {
        if (product.getStatus() == null) {
            product.setStatus(2);
        }
        productMapper.insert(product);
        return product;
    }

    public void updateProductStatus(Long productId, Integer status, Long sellerId, boolean isAdmin) {
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<Product>()
            .eq(Product::getId, productId)
            .set(Product::getStatus, status);
        if (!isAdmin) {
            wrapper.eq(Product::getSellerId, sellerId);
        }
        productMapper.update(null, wrapper);
    }

    public void updateProduct(Product product, Long sellerId, boolean isAdmin) {
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<Product>()
            .eq(Product::getId, product.getId());
        if (!isAdmin) {
            wrapper.eq(Product::getSellerId, sellerId);
        }
        product.setId(null);
        product.setSellerId(null);
        productMapper.update(product, wrapper);
    }

    public void deleteProduct(Long productId, Long sellerId, boolean isAdmin) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getId, productId);
        if (!isAdmin) {
            wrapper.eq(Product::getSellerId, sellerId);
        }
        productMapper.delete(wrapper);
    }

    public Map<String, Object> getSellerStats(Long sellerId, boolean isAdmin) {
        Map<String, Object> stats = new HashMap<>();

        List<Product> products = isAdmin ? getAllProducts() : getMyProducts(sellerId);
        stats.put("totalProducts", products.size());
        stats.put("onShelf", products.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count());
        stats.put("offShelf", products.stream().filter(p -> p.getStatus() != null && p.getStatus() == 0).count());
        stats.put("pendingReview", products.stream().filter(p -> p.getStatus() != null && p.getStatus() == 2).count());

        List<Long> productIds = new ArrayList<>();
        for (Product p : products) {
            productIds.add(p.getId());
        }

        long cartCount = 0;
        long purchaseCount = 0;
        long viewCount = 0;

        if (!productIds.isEmpty()) {
            cartCount = behaviorLogMapper.selectCount(
                new LambdaQueryWrapper<UserBehaviorLog>()
                    .eq(UserBehaviorLog::getEventType, "CART")
                    .in(UserBehaviorLog::getProductId, productIds)
            );
            purchaseCount = behaviorLogMapper.selectCount(
                new LambdaQueryWrapper<UserBehaviorLog>()
                    .eq(UserBehaviorLog::getEventType, "PURCHASE")
                    .in(UserBehaviorLog::getProductId, productIds)
            );
            viewCount = behaviorLogMapper.selectCount(
                new LambdaQueryWrapper<UserBehaviorLog>()
                    .eq(UserBehaviorLog::getEventType, "VIEW")
                    .in(UserBehaviorLog::getProductId, productIds)
            );
        }

        stats.put("cartCount", cartCount);
        stats.put("purchaseCount", purchaseCount);
        stats.put("viewCount", viewCount);

        return stats;
    }

    public List<Map<String, Object>> getProductBehaviorStats(Long sellerId, boolean isAdmin) {
        List<Product> products = isAdmin ? getAllProducts() : getMyProducts(sellerId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Product p : products) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", p.getId());
            item.put("productName", p.getName());
            item.put("category", p.getCategory());
            item.put("price", p.getPrice());
            item.put("stock", p.getStock());
            item.put("status", p.getStatus());

            long views = behaviorLogMapper.selectCount(
                new LambdaQueryWrapper<UserBehaviorLog>()
                    .eq(UserBehaviorLog::getEventType, "VIEW")
                    .eq(UserBehaviorLog::getProductId, p.getId())
            );
            long carts = behaviorLogMapper.selectCount(
                new LambdaQueryWrapper<UserBehaviorLog>()
                    .eq(UserBehaviorLog::getEventType, "CART")
                    .eq(UserBehaviorLog::getProductId, p.getId())
            );
            long purchases = behaviorLogMapper.selectCount(
                new LambdaQueryWrapper<UserBehaviorLog>()
                    .eq(UserBehaviorLog::getEventType, "PURCHASE")
                    .eq(UserBehaviorLog::getProductId, p.getId())
            );

            item.put("views", views);
            item.put("carts", carts);
            item.put("purchases", purchases);
            result.add(item);
        }

        result.sort((a, b) -> Long.compare(
            (long) b.get("carts") + (long) b.get("purchases"),
            (long) a.get("carts") + (long) a.get("purchases")
        ));

        return result;
    }
}
