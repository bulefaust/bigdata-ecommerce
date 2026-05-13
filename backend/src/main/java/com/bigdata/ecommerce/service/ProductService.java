package com.bigdata.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public Page<Product> listProducts(int pageNum, int pageSize, String category) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Product::getStatus, 2);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(page, wrapper);
    }

    public Page<Product> searchProducts(int pageNum, int pageSize, String keyword) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Product::getStatus, 2);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                   .or()
                   .like(Product::getDescription, keyword)
                   .or()
                   .like(Product::getCategory, keyword));
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(page, wrapper);
    }

    public Product getProductById(Long id, Long userId) {
        return productMapper.selectById(id);
    }

    public void clickProduct(Long userId, Long productId) {
    }

    public void searchProduct(Long userId, String keyword) {
    }

    public List<Product> getHotProducts(int limit) {
        LambdaQueryWrapper<Product> countWrapper = new LambdaQueryWrapper<>();
        Long total = productMapper.selectCount(countWrapper);
        if (total <= limit) {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Product::getCreateTime);
            wrapper.last("LIMIT " + limit);
            return productMapper.selectList(wrapper);
        }
        Random random = new Random();
        int offset = random.nextInt((int) (total - limit));
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT " + limit + " OFFSET " + offset);
        return productMapper.selectList(wrapper);
    }

    public List<Product> getRecommendByCategory(String category, Long excludeId, int limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        if (excludeId != null) {
            wrapper.ne(Product::getId, excludeId);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        wrapper.last("LIMIT " + limit);
        List<Product> products = productMapper.selectList(wrapper);
        if (products.size() < limit) {
            LambdaQueryWrapper<Product> fillWrapper = new LambdaQueryWrapper<>();
            if (excludeId != null) {
                fillWrapper.ne(Product::getId, excludeId);
            }
            fillWrapper.notIn(Product::getId, products.stream().map(Product::getId).collect(Collectors.toList()));
            fillWrapper.orderByDesc(Product::getCreateTime);
            fillWrapper.last("LIMIT " + (limit - products.size()));
            products.addAll(productMapper.selectList(fillWrapper));
        }
        return products;
    }

    public List<Product> getSimilarProducts(Long productId, int limit) {
        Product current = productMapper.selectById(productId);
        if (current == null) {
            return getHotProducts(limit);
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategory, current.getCategory());
        wrapper.ne(Product::getId, productId);
        wrapper.orderByDesc(Product::getCreateTime);
        wrapper.last("LIMIT " + limit);
        List<Product> similar = productMapper.selectList(wrapper);
        if (similar.size() < limit) {
            LambdaQueryWrapper<Product> fillWrapper = new LambdaQueryWrapper<>();
            fillWrapper.ne(Product::getId, productId);
            fillWrapper.notIn(Product::getId, similar.stream().map(Product::getId).collect(Collectors.toList()));
            fillWrapper.orderByDesc(Product::getCreateTime);
            fillWrapper.last("LIMIT " + (limit - similar.size()));
            similar.addAll(productMapper.selectList(fillWrapper));
        }
        return similar;
    }

    public List<Product> getRandomRecommend(int limit) {
        LambdaQueryWrapper<Product> countWrapper = new LambdaQueryWrapper<>();
        Long total = productMapper.selectCount(countWrapper);
        if (total <= limit) {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.last("LIMIT " + limit);
            return productMapper.selectList(wrapper);
        }
        Random random = new Random();
        int offset = random.nextInt((int) (total - limit));
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreateTime);
        wrapper.last("LIMIT " + limit + " OFFSET " + offset);
        return productMapper.selectList(wrapper);
    }
}
