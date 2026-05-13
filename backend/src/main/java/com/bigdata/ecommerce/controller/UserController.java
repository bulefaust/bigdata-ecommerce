package com.bigdata.ecommerce.controller;

import com.bigdata.ecommerce.entity.User;
import com.bigdata.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody Map<String, String> body) {
        return userService.login(body.get("username"), body.get("password"));
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/list")
    public List<User> list() {
        return userService.listUsers();
    }

    @PostMapping("/update-role")
    public User updateRole(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String role = body.get("role").toString();
        return userService.updateRole(id, role);
    }
}
