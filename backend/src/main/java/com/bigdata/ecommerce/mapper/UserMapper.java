package com.bigdata.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigdata.ecommerce.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
