package com.bigdata.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_realtime_hot")
public class RealtimeHot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String productName;
    private String category;
    private String imageUrl;
    private BigDecimal price;
    private Integer clicks;
    private Integer purchases;
    private Integer carts;
    private Integer favorites;
    private Double hotScore;
    private Integer rank;
    private String timeWindow;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
