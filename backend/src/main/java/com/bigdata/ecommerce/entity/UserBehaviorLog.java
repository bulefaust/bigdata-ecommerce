package com.bigdata.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_user_behavior_log")
public class UserBehaviorLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String eventType;
    private Long productId;
    private String category;
    private String keyword;
    private Integer duration;
    private Long timestamp;
}
