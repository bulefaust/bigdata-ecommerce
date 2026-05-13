package com.bigdata.ecommerce.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigdata.ecommerce.entity.RealtimeHot;
import com.bigdata.ecommerce.mapper.RealtimeHotMapper;
import com.bigdata.ecommerce.mapper.UserBehaviorLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private RealtimeHotMapper realtimeHotMapper;

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    @GetMapping("/hot")
    public Map<String, Object> getHotProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "1h") String timeRange) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LambdaQueryWrapper<RealtimeHot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RealtimeHot::getTimeWindow, timeRange);
            wrapper.orderByDesc(RealtimeHot::getHotScore);
            wrapper.last("LIMIT " + limit);
            
            List<RealtimeHot> hotProducts = realtimeHotMapper.selectList(wrapper);
            result.put("products", hotProducts != null ? hotProducts : new ArrayList<>());
            result.put("timeRange", timeRange);
            result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (Exception e) {
            result.put("products", new ArrayList<>());
            result.put("timeRange", timeRange);
            result.put("updateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return result;
    }

    @GetMapping("/user-activity")
    public Map<String, Object> getUserActivity(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(30);
        
        try {
            List<Map<String, Object>> dailyActive = behaviorLogMapper.getDailyActiveUsers(start.toString(), end.plusDays(1).toString());
            result.put("dailyActive", dailyActive != null ? dailyActive : new ArrayList<>());
            
            List<Map<String, Object>> hourlyActive = behaviorLogMapper.getHourlyDistribution();
            result.put("hourlyDistribution", hourlyActive != null ? hourlyActive : new ArrayList<>());
            
            List<Map<String, Object>> allStats = behaviorLogMapper.getDailyActiveUsers(start.toString(), end.plusDays(1).toString());
            long totalBehaviors = 0;
            if (allStats != null) {
                for (Map<String, Object> day : allStats) {
                    Object dau = day.get("dau");
                    if (dau instanceof Number) {
                        totalBehaviors += ((Number) dau).longValue();
                    }
                }
            }
            result.put("totalBehaviors", totalBehaviors);
        } catch (Exception e) {
            result.put("dailyActive", new ArrayList<>());
            result.put("hourlyDistribution", new ArrayList<>());
            result.put("totalBehaviors", 0);
        }
        
        return result;
    }

    @GetMapping("/retention")
    public Map<String, Object> getUserRetention(
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> retentionData = behaviorLogMapper.getUserRetention(days);
            result.put("retention", retentionData != null ? retentionData : new ArrayList<>());
        } catch (Exception e) {
            result.put("retention", new ArrayList<>());
        }
        result.put("days", days);
        
        return result;
    }

    @GetMapping("/funnel")
    public Map<String, Object> getConversionFunnel(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(30);
        
        Map<String, Integer> funnel = new LinkedHashMap<>();
        funnel.put("浏览商品", 0);
        funnel.put("点击商品", 0);
        funnel.put("加入购物车", 0);
        funnel.put("提交订单", 0);
        funnel.put("完成支付", 0);
        
        try {
            List<Map<String, Object>> funnelData = behaviorLogMapper.getConversionFunnel(start.toString(), end.toString());
            
            if (funnelData != null) {
                for (Map<String, Object> item : funnelData) {
                    String eventType = (String) item.get("event_type");
                    Long count = (Long) item.get("cnt");
                    if (eventType != null && count != null) {
                        switch (eventType.toUpperCase()) {
                            case "VIEW": funnel.put("浏览商品", count.intValue()); break;
                            case "CLICK": funnel.put("点击商品", count.intValue()); break;
                            case "CART": funnel.put("加入购物车", count.intValue()); break;
                            case "PURCHASE": funnel.put("提交订单", count.intValue()); break;
                            case "PAY": funnel.put("完成支付", count.intValue()); break;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        
        result.put("funnel", funnel);
        result.put("startDate", start.toString());
        result.put("endDate", end.toString());
        
        List<Map<String, Object>> conversionRates = new ArrayList<>();
        Object[] keys = funnel.keySet().toArray();
        for (int i = 0; i < keys.length - 1; i++) {
            int current = funnel.get(keys[i]);
            int next = funnel.get(keys[i + 1]);
            double rate = current > 0 ? (next * 100.0 / current) : 0;
            Map<String, Object> rateItem = new HashMap<>();
            rateItem.put("step", keys[i] + " -> " + keys[i + 1]);
            rateItem.put("rate", String.format("%.2f%%", rate));
            conversionRates.add(rateItem);
        }
        result.put("conversionRates", conversionRates);
        
        return result;
    }

    @GetMapping("/sales-forecast")
    public Map<String, Object> getSalesForecast(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days * 2);
        
        List<Map<String, Object>> historical = new ArrayList<>();
        try {
            historical = behaviorLogMapper.getDailySales(start.toString(), today.toString());
            if (historical == null) historical = new ArrayList<>();
        } catch (Exception e) {
            historical = new ArrayList<>();
        }
        result.put("historical", historical);
        
        List<Map<String, Object>> forecast = calculateForecast(historical, days);
        result.put("forecast", forecast);
        
        double totalSales = 0;
        for (Map<String, Object> day : historical) {
            Object purchases = day.get("purchases");
            if (purchases instanceof Number) {
                totalSales += ((Number) purchases).doubleValue();
            }
        }
        double avgDaily = totalSales / Math.max(historical.size(), 1);
        double trend = calculateTrend(historical);
        
        result.put("avgDaily", (int) avgDaily);
        result.put("trend", trend > 0 ? "上升" : (trend < 0 ? "下降" : "稳定"));
        result.put("trendValue", String.format("%.1f%%", Math.abs(trend)));
        
        return result;
    }

    private List<Map<String, Object>> calculateForecast(List<Map<String, Object>> historical, int forecastDays) {
        List<Map<String, Object>> forecast = new ArrayList<>();
        
        for (int i = 1; i <= forecastDays; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", LocalDate.now().plusDays(i).toString());
            
            double predicted = 0;
            if (historical.size() >= 7) {
                double sum = 0;
                for (int j = historical.size() - 7; j < historical.size(); j++) {
                    Object purchases = historical.get(j).get("purchases");
                    if (purchases instanceof Number) {
                        sum += ((Number) purchases).doubleValue();
                    }
                }
                double weeklyAvg = sum / 7;
                double growthRate = calculateGrowthRate(historical);
                predicted = weeklyAvg * Math.pow(1 + growthRate / 100, i);
            }
            
            day.put("predicted", (int) predicted);
            day.put("type", "forecast");
            forecast.add(day);
        }
        
        return forecast;
    }

    private double calculateTrend(List<Map<String, Object>> historical) {
        if (historical.size() < 2) return 0;
        
        double first = 0, last = 0;
        int count = historical.size();
        
        for (int i = 0; i < Math.min(7, count); i++) {
            Object purchases = historical.get(i).get("purchases");
            if (purchases instanceof Number) {
                first += ((Number) purchases).doubleValue();
            }
        }
        
        for (int i = Math.max(0, count - 7); i < count; i++) {
            Object purchases = historical.get(i).get("purchases");
            if (purchases instanceof Number) {
                last += ((Number) purchases).doubleValue();
            }
        }
        
        return first > 0 ? ((last - first) / first * 100) : 0;
    }

    private double calculateGrowthRate(List<Map<String, Object>> historical) {
        if (historical.size() < 14) return 0;
        
        double firstHalf = 0, secondHalf = 0;
        int mid = historical.size() / 2;
        
        for (int i = 0; i < mid; i++) {
            Object purchases = historical.get(i).get("purchases");
            if (purchases instanceof Number) {
                firstHalf += ((Number) purchases).doubleValue();
            }
        }
        
        for (int i = mid; i < historical.size(); i++) {
            Object purchases = historical.get(i).get("purchases");
            if (purchases instanceof Number) {
                secondHalf += ((Number) purchases).doubleValue();
            }
        }
        
        return firstHalf > 0 ? ((secondHalf - firstHalf) / firstHalf * 100 / mid * 7) : 0;
    }

    @PostMapping("/update-hot")
    public Map<String, Object> updateHotProducts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            String timeWindow = "1h";
            
            List<Map<String, Object>> hotData = behaviorLogMapper.getHotProducts("VIEW", timeWindow);
            
            if (hotData != null && !hotData.isEmpty()) {
                realtimeHotMapper.delete(new LambdaQueryWrapper<RealtimeHot>()
                    .eq(RealtimeHot::getTimeWindow, timeWindow));
                
                for (int i = 0; i < Math.min(hotData.size(), 50); i++) {
                    Map<String, Object> item = hotData.get(i);
                    RealtimeHot hot = new RealtimeHot();
                    hot.setProductId(((Number) item.get("product_id")).longValue());
                    hot.setCategory((String) item.get("category"));
                    Object clicks = item.get("clicks");
                    Object purchases = item.get("purchases");
                    Object carts = item.get("carts");
                    Object favorites = item.get("favorites");
                    hot.setClicks(clicks != null ? ((Number) clicks).intValue() : 0);
                    hot.setPurchases(purchases != null ? ((Number) purchases).intValue() : 0);
                    hot.setCarts(carts != null ? ((Number) carts).intValue() : 0);
                    hot.setFavorites(favorites != null ? ((Number) favorites).intValue() : 0);
                    double score = hot.getClicks() * 1.0 + hot.getPurchases() * 5.0 + hot.getCarts() * 3.0 + hot.getFavorites() * 2.0;
                    hot.setHotScore(score);
                    hot.setRank(i + 1);
                    hot.setTimeWindow(timeWindow);
                    hot.setUpdateTime(now);
                    realtimeHotMapper.insert(hot);
                }
                
                result.put("success", true);
                result.put("updated", hotData.size());
            } else {
                result.put("success", true);
                result.put("updated", 0);
                result.put("message", "暂无行为数据");
            }
            result.put("time", now.toString());
        } catch (Exception e) {
            result.put("success", false);
            result.put("updated", 0);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
