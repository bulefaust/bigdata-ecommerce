package com.bigdata.ecommerce.controller;

import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.entity.User;
import com.bigdata.ecommerce.service.SellerService;
import com.bigdata.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private UserService userService;

    private boolean isSellerOrAdmin(Long userId) {
        if (userId == null) return false;
        User user = userService.getUserById(userId);
        return user != null && ("seller".equals(user.getRole()) || "admin".equals(user.getRole()));
    }

    private boolean isAdmin(Long userId) {
        if (userId == null) return false;
        User user = userService.getUserById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    @GetMapping("/products")
    public List<Product> getMyProducts(@RequestParam Long userId) {
        if (isAdmin(userId)) {
            return sellerService.getAllProducts();
        }
        return sellerService.getMyProducts(userId);
    }

    @PostMapping("/product")
    public Product addProduct(@RequestBody Product product) {
        return sellerService.addProduct(product);
    }

    @PutMapping("/product/{id}/status")
    public void updateStatus(@PathVariable Long id, @RequestParam Integer status,
                             @RequestParam Long userId) {
        sellerService.updateProductStatus(id, status, userId, isAdmin(userId));
    }

    @PutMapping("/product")
    public void updateProduct(@RequestBody Product product, @RequestParam Long userId) {
        sellerService.updateProduct(product, userId, isAdmin(userId));
    }

    @DeleteMapping("/product/{id}")
    public void deleteProduct(@PathVariable Long id, @RequestParam Long userId) {
        sellerService.deleteProduct(id, userId, isAdmin(userId));
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats(@RequestParam Long userId) {
        return sellerService.getSellerStats(userId, isAdmin(userId));
    }

    @GetMapping("/product-behavior")
    public List<Map<String, Object>> getProductBehaviorStats(@RequestParam Long userId) {
        return sellerService.getProductBehaviorStats(userId, isAdmin(userId));
    }

    @GetMapping("/pending-products")
    public List<Product> getPendingProducts(@RequestParam Long userId) {
        if (!isAdmin(userId)) {
            return java.util.Collections.emptyList();
        }
        return sellerService.getPendingProducts();
    }

    @PutMapping("/product/{id}/approve")
    public void approveProduct(@PathVariable Long id, @RequestParam Long userId, @RequestParam Integer status) {
        if (!isAdmin(userId)) return;
        sellerService.updateProductStatus(id, status, userId, true);
    }
}
