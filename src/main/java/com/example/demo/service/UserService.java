package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.GoogleLoginDTO;
import com.example.demo.dto.ProfileUpdateDTO;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.vo.UserVO;

public interface UserService extends IService<User> {

    /**
     * 管理员分页/筛选查询用户列表
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param role   可选角色过滤
     * @param status 可选状态过滤
     */
    IPage<UserVO> listUsers(int page, int size, String role, String status);

    
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

    /**
     * 注销当前用户账号（软删除，将账号状态置为 suspended）
     * @param userId 用户 ID
     */
    void deactivateAccount(Long userId);

    /**
     * 管理员更新用户状态
     * @param userId 用户 ID
     * @param status 新的账号状态
     */
    void updateUserStatus(Long userId, String status);

    /**
     * Google 第三方登录
     * @param googleLoginDTO 包含 Google ID Token
     * @return 用户信息（含 JWT Token）
     */
    UserVO googleLogin(GoogleLoginDTO googleLoginDTO);

    /**
     * 更新用户资料（生日、地址）
     * @param userId 用户 ID
     * @param profileUpdateDTO 资料更新信息
     * @return 更新后的用户信息
     */
    UserVO updateProfile(Long userId, ProfileUpdateDTO profileUpdateDTO);

    /**
     * 验证邮箱验证码
     * @param email 用户邮箱
     * @param code 6位验证码
     */
    void verifyEmail(String email, String code);

    /**
     * 重新发送邮箱验证码
     * @param email 用户邮箱
     */
    void resendVerificationCode(String email);
}
