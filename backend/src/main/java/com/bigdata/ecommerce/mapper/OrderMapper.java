package com.bigdata.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.ecommerce.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
