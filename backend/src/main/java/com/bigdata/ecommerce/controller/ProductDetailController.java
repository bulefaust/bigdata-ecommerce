package com.bigdata.ecommerce.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdata.ecommerce.entity.ProductSpec;
import com.bigdata.ecommerce.entity.ProductReview;
import com.bigdata.ecommerce.mapper.ProductSpecMapper;
import com.bigdata.ecommerce.mapper.ProductReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductDetailController {

    @Autowired
    private ProductSpecMapper specMapper;

    @Autowired
    private ProductReviewMapper reviewMapper;

    @GetMapping("/{id}/specs")
    public List<ProductSpec> getSpecs(@PathVariable Long id) {
        LambdaQueryWrapper<ProductSpec> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSpec::getProductId, id);
        return specMapper.selectList(wrapper);
    }

    @GetMapping("/{id}/reviews")
    public List<ProductReview> getReviews(@PathVariable Long id) {
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductReview::getProductId, id);
        wrapper.orderByDesc(ProductReview::getCreateTime);
        return reviewMapper.selectList(wrapper);
    }
}
