package com.bigdata.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_product_spec")
public class ProductSpec {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String specName;
    private String specValue;
}
