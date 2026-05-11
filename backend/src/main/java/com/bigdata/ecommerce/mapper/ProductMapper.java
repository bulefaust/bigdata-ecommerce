package com.bigdata.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.ecommerce.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
