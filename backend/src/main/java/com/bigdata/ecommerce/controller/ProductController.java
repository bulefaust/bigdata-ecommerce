package com.bigdata.ecommerce.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Page<Product> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        return productService.listProducts(pageNum, pageSize, category);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id,
                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return productService.getProductById(id, userId);
    }

    @PostMapping("/click")
    public String click(@RequestBody Map<String, Long> body) {
        productService.clickProduct(body.get("userId"), body.get("productId"));
        return "ok";
    }

    @GetMapping("/search")
    public Page<Product> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        productService.searchProduct(userId, keyword);
        return productService.searchProducts(pageNum, pageSize, keyword);
    }

    @GetMapping("/hot")
    public List<Product> hot(@RequestParam(defaultValue = "8") int limit) {
        return productService.getHotProducts(limit);
    }

    @GetMapping("/recommend")
    public List<Product> recommend(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long excludeId,
            @RequestParam(defaultValue = "8") int limit) {
        return productService.getRecommendByCategory(category, excludeId, limit);
    }

    @GetMapping("/similar/{id}")
    public List<Product> similar(@PathVariable Long id,
                                  @RequestParam(defaultValue = "6") int limit) {
        return productService.getSimilarProducts(id, limit);
    }

    @GetMapping("/random")
    public List<Product> random(@RequestParam(defaultValue = "8") int limit) {
        return productService.getRandomRecommend(limit);
    }
}
