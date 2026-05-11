package com.bigdata.ecommerce.service;

import com.bigdata.ecommerce.entity.User;
import com.bigdata.ecommerce.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public User register(User user) {
        userMapper.insert(user);
        return user;
    }

    public User login(String username, String password) {
        return userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password)
        );
    }
}
