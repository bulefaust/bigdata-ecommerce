package com.bigdata.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.ecommerce.entity.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;
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
}
