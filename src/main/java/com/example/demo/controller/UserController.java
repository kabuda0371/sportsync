package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.service.UserService;

import com.example.demo.common.Result;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<User>> listAllUsers() {
        return Result.success(userMapper.selectList(null));
    }

    @PostMapping("/register")
    public Result<User> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        System.out.println(111);
        try {
            System.out.println(registerDTO);
            User savedUser = userService.register(registerDTO);

            savedUser.setPasswordHash(null);
            
            return Result.success("用户注册成功", savedUser);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<User> loginUser(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            User user = userService.login(loginDTO);
            user.setPasswordHash(null); // 不要将密码哈希返回给前端
            return Result.success("登录成功", user);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
