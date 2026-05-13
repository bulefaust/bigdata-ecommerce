package com.bigdata.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_product_review")
public class ProductReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private Integer rating;
    private String content;
    private String createTime;
}
