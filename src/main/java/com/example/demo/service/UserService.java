package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.entity.User;

public interface UserService extends IService<User> {
    
    /**
     * 注册新用户
     * @param registerDTO 注册信息DTO
     * @return 注册成功的用户信息
     */
    User register(UserRegisterDTO registerDTO);
    /**
     * 用户登录
     * @param loginDTO 登录信息DTO
     * @return 登录成功的用户信息
     */
    User login(com.example.demo.dto.UserLoginDTO loginDTO);
}
