package com.bigdata.ecommerce.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdata.ecommerce.entity.Order;
import com.bigdata.ecommerce.entity.Product;
import com.bigdata.ecommerce.entity.UserBehaviorLog;
import com.bigdata.ecommerce.mapper.OrderMapper;
import com.bigdata.ecommerce.mapper.ProductMapper;
import com.bigdata.ecommerce.mapper.UserBehaviorLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        Map<String, Object> data = new HashMap<>();

        Long totalOrders = orderMapper.selectCount(new LambdaQueryWrapper<>());
        data.put("totalOrders", totalOrders);

        LambdaQueryWrapper<Order> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.eq(Order::getStatus, 1).or().eq(Order::getStatus, 2).or().eq(Order::getStatus, 3);
        List<Order> paidOrders = orderMapper.selectList(paidWrapper);
        BigDecimal totalRevenue = paidOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalRevenue", totalRevenue);

        Long totalProducts = productMapper.selectCount(new LambdaQueryWrapper<>());
        data.put("totalProducts", totalProducts);

        Long totalBehaviors = behaviorLogMapper.selectCount(new LambdaQueryWrapper<>());
        data.put("totalBehaviors", totalBehaviors);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Order::getCreateTime, todayStart);
        Long todayOrders = orderMapper.selectCount(todayWrapper);
        data.put("todayOrders", todayOrders);

        LambdaQueryWrapper<Order> todayPaidWrapper = new LambdaQueryWrapper<>();
        todayPaidWrapper.ge(Order::getCreateTime, todayStart);
        todayPaidWrapper.in(Order::getStatus, 1, 2, 3);
        List<Order> todayPaidOrders = orderMapper.selectList(todayPaidWrapper);
        BigDecimal todayRevenue = todayPaidOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("todayRevenue", todayRevenue);

        LambdaQueryWrapper<UserBehaviorLog> todayBehaviorWrapper = new LambdaQueryWrapper<>();
        todayBehaviorWrapper.ge(UserBehaviorLog::getTimestamp, todayStart.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        Long todayBehaviors = behaviorLogMapper.selectCount(todayBehaviorWrapper);
        data.put("todayBehaviors", todayBehaviors);

        return data;
    }

    @GetMapping("/revenue-trend")
    public List<Map<String, Object>> revenueTrend(@RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Order::getCreateTime, dayStart);
            wrapper.lt(Order::getCreateTime, dayEnd);
            wrapper.in(Order::getStatus, 1, 2, 3);
            List<Order> dayOrders = orderMapper.selectList(wrapper);

            BigDecimal revenue = dayOrders.stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(fmt));
            item.put("revenue", revenue);
            item.put("orderCount", dayOrders.size());
            result.add(item);
        }
        return result;
    }

    @GetMapping("/category-stats")
    public List<Map<String, Object>> categoryStats() {
        List<Product> allProducts = productMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, Long> categoryCount = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("category", e.getKey());
                    item.put("count", e.getValue());
                    result.add(item);
                });
        return result;
    }

    @GetMapping("/behavior-stats")
    public List<Map<String, Object>> behaviorStats() {
        List<UserBehaviorLog> allLogs = behaviorLogMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, Long> typeCount = allLogs.stream()
                .collect(Collectors.groupingBy(UserBehaviorLog::getEventType, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        typeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", e.getKey());
                    item.put("count", e.getValue());
                    result.add(item);
                });
        return result;
    }

    @GetMapping("/hot-products")
    public List<Map<String, Object>> hotProducts() {
        List<UserBehaviorLog> allLogs = behaviorLogMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, Long> productClicks = allLogs.stream()
                .filter(l -> l.getProductId() != null)
                .collect(Collectors.groupingBy(UserBehaviorLog::getProductId, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        productClicks.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> {
                    Product p = productMapper.selectById(e.getKey());
                    if (p != null) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("productId", p.getId());
                        item.put("productName", p.getName());
                        item.put("category", p.getCategory());
                        item.put("price", p.getPrice());
                        item.put("clickCount", e.getValue());
                        result.add(item);
                    }
                });
        return result;
    }

    @GetMapping("/realtime-orders")
    public List<Map<String, Object>> realtimeOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreateTime);
        wrapper.last("LIMIT 20");
        List<Order> orders = orderMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> item = new HashMap<>();
            item.put("orderId", o.getId());
            item.put("userId", o.getUserId());
            item.put("amount", o.getTotalAmount());
            item.put("status", o.getStatus());
            item.put("time", o.getCreateTime() != null ? o.getCreateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "");

            LambdaQueryWrapper<UserBehaviorLog> purchaseWrapper = new LambdaQueryWrapper<>();
            purchaseWrapper.eq(UserBehaviorLog::getUserId, o.getUserId());
            purchaseWrapper.eq(UserBehaviorLog::getEventType, "PURCHASE");
            if (o.getCreateTime() != null) {
                long before = o.getCreateTime().minusSeconds(60).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                long after = o.getCreateTime().plusSeconds(60).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                purchaseWrapper.ge(UserBehaviorLog::getTimestamp, before);
                purchaseWrapper.le(UserBehaviorLog::getTimestamp, after);
            }
            purchaseWrapper.last("LIMIT 1");
            List<UserBehaviorLog> logs = behaviorLogMapper.selectList(purchaseWrapper);

            String productName = "商品订单";
            if (!logs.isEmpty() && logs.get(0).getProductId() != null) {
                Product p = productMapper.selectById(logs.get(0).getProductId());
                if (p != null) productName = p.getName();
            }
            item.put("productName", productName);
            result.add(item);
        }
        return result;
    }
}
