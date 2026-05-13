package com.bigdata.ecommerce.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdata.ecommerce.entity.FlashSale;
import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.mapper.FlashSaleMapper;
import com.bigdata.ecommerce.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FlashSaleService {

    @Autowired
    private FlashSaleMapper flashSaleMapper;

    @Autowired
    private ProductMapper productMapper;

    public List<Map<String, Object>> getCurrentFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<FlashSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSale::getStatus, 1);
        wrapper.le(FlashSale::getStartTime, now);
        wrapper.ge(FlashSale::getEndTime, now);
        wrapper.orderByAsc(FlashSale::getStartTime);
        List<FlashSale> sales = flashSaleMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (FlashSale sale : sales) {
            Product product = productMapper.selectById(sale.getProductId());
            if (product != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", sale.getId());
                item.put("productId", product.getId());
                item.put("productName", product.getName());
                item.put("productImage", product.getImageUrl());
                item.put("category", product.getCategory());
                item.put("originalPrice", sale.getOriginalPrice());
                item.put("salePrice", sale.getSalePrice());
                item.put("totalStock", sale.getTotalStock());
                item.put("remainStock", sale.getRemainStock());
                item.put("startTime", sale.getStartTime().toString());
                item.put("endTime", sale.getEndTime().toString());
                item.put("title", sale.getTitle());
                item.put("discount", Math.round(sale.getSalePrice().doubleValue() / sale.getOriginalPrice().doubleValue() * 10) / 10.0);
                result.add(item);
            }
        }
        return result;
    }

    public List<Map<String, Object>> getUpcomingFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<FlashSale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlashSale::getStatus, 1);
        wrapper.gt(FlashSale::getStartTime, now);
        wrapper.orderByAsc(FlashSale::getStartTime);
        wrapper.last("LIMIT 4");
        List<FlashSale> sales = flashSaleMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (FlashSale sale : sales) {
            Product product = productMapper.selectById(sale.getProductId());
            if (product != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", sale.getId());
                item.put("productId", product.getId());
                item.put("productName", product.getName());
                item.put("productImage", product.getImageUrl());
                item.put("originalPrice", sale.getOriginalPrice());
                item.put("salePrice", sale.getSalePrice());
                item.put("startTime", sale.getStartTime().toString());
                item.put("endTime", sale.getEndTime().toString());
                item.put("title", sale.getTitle());
                result.add(item);
            }
        }
        return result;
    }
}
