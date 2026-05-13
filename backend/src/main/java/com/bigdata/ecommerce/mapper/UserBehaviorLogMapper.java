package com.bigdata.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.ecommerce.entity.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserBehaviorLogMapper extends BaseMapper<UserBehaviorLog> {

    @Select("SELECT category, COUNT(*) as cnt FROM t_user_behavior_log WHERE user_id = #{userId} AND category IS NOT NULL GROUP BY category ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> getUserCategoryStats(Long userId, int limit);

    @Select("SELECT product_id, COUNT(*) as cnt FROM t_user_behavior_log WHERE user_id = #{userId} AND product_id IS NOT NULL GROUP BY product_id ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> getUserProductStats(Long userId, int limit);

    @Select("SELECT DISTINCT product_id FROM t_user_behavior_log WHERE user_id IN (SELECT user_id FROM t_user_behavior_log WHERE product_id IN (SELECT product_id FROM t_user_behavior_log WHERE user_id = #{userId} AND product_id IS NOT NULL) AND user_id != #{userId} GROUP BY user_id ORDER BY COUNT(*) DESC LIMIT 20) AND product_id NOT IN (SELECT product_id FROM t_user_behavior_log WHERE user_id = #{userId} AND product_id IS NOT NULL) AND product_id IS NOT NULL GROUP BY product_id ORDER BY COUNT(*) DESC LIMIT #{limit}")
    List<Long> getCollaborativeProducts(Long userId, int limit);

    @Select("SELECT DATE(create_time) as date, COUNT(DISTINCT user_id) as dau FROM t_user_behavior_log WHERE create_time >= #{startDate} AND create_time < #{endDate} GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> getDailyActiveUsers(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select("SELECT HOUR(create_time) as hour, COUNT(*) as cnt FROM t_user_behavior_log WHERE DATE(create_time) = CURDATE() GROUP BY HOUR(create_time) ORDER BY hour")
    List<Map<String, Object>> getHourlyDistribution();

    @Select("SELECT event_type, COUNT(*) as cnt FROM t_user_behavior_log WHERE create_time >= #{startDate} AND create_time < #{endDate} GROUP BY event_type")
    List<Map<String, Object>> getConversionFunnel(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select("SELECT DATE(create_time) as date, SUM(CASE WHEN event_type = 'PURCHASE' THEN 1 ELSE 0 END) as purchases, SUM(CASE WHEN event_type = 'VIEW' THEN 1 ELSE 0 END) as views FROM t_user_behavior_log WHERE create_time >= #{startDate} AND create_time < #{endDate} GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> getDailySales(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select("SELECT DATEDIFF(CURDATE(), DATE(MIN(create_time))) as days_since_join, COUNT(DISTINCT user_id) as total_users, SUM(CASE WHEN create_time >= DATE_SUB(CURDATE(), INTERVAL 1 DAY) THEN 1 ELSE 0 END) as d1_retained, SUM(CASE WHEN create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) as d7_retained FROM t_user_behavior_log")
    List<Map<String, Object>> getUserRetention(@Param("days") int days);

    @Select("SELECT product_id, SUM(CASE WHEN event_type = 'VIEW' THEN 1 ELSE 0 END) as views, SUM(CASE WHEN event_type = 'CLICK' THEN 1 ELSE 0 END) as clicks, SUM(CASE WHEN event_type = 'PURCHASE' THEN 1 ELSE 0 END) as purchases, SUM(CASE WHEN event_type = 'CART' THEN 1 ELSE 0 END) as carts, SUM(CASE WHEN event_type = 'FAVORITE' THEN 1 ELSE 0 END) as favorites FROM t_user_behavior_log WHERE event_type IN ('VIEW', 'CLICK', 'PURCHASE', 'CART', 'FAVORITE') AND create_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR) GROUP BY product_id ORDER BY (SUM(CASE WHEN event_type = 'CLICK' THEN 1 ELSE 0 END) * 1.0 + SUM(CASE WHEN event_type = 'PURCHASE' THEN 5 ELSE 0 END) + SUM(CASE WHEN event_type = 'CART' THEN 3 ELSE 0 END) + SUM(CASE WHEN event_type = 'FAVORITE' THEN 2 ELSE 0 END)) DESC")
    List<Map<String, Object>> getHotProducts(@Param("eventType") String eventType, @Param("timeWindow") String timeWindow);
}
