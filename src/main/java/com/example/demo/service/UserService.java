package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.vo.UserVO;

public interface UserService extends IService<User> {
    
    /**
     * 注册新用户
     * @param registerDTO 注册信息DTO
     * @return 注册成功的用户信息
     */
    UserVO register(UserRegisterDTO registerDTO);
    /**
     * 用户登录
     * @param loginDTO 登录信息DTO
     * @return 登录成功的用户信息
     */
    UserVO login(com.example.demo.dto.UserLoginDTO loginDTO);

    /**
     * 获取当前登录用户信息
     * @param userId 用户 ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long userId);
}
