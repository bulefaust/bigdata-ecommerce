package com.bigdata.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_flash_sale")
public class FlashSale {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer totalStock;
    private Integer remainStock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String title;
    private Integer status;
}
