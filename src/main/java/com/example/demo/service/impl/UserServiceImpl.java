package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User register(UserRegisterDTO registerDTO) {
        // 1. 检查邮箱是否已被注册
        // 使用 lambdaQuery() 避免硬编码 "email" 字符串，提高类型安全
        if (this.lambdaQuery().eq(User::getEmail, registerDTO.getEmail()).count() > 0) {
            throw new RuntimeException("该邮箱已被注册！");
        }

        // 2. 密码加密 (使用 jbcrypt)
        String encryptedPassword = BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt());

        // 3. 构建用户实体并保存
        User user = new User();
        user.setEmail(registerDTO.getEmail());
        user.setPasswordHash(encryptedPassword); // 密码密文存入 passwordHash 字段
        user.setName(registerDTO.getName());
        
        // constraint users_chk_1: role in ('member', 'staff', 'admin')
        user.setRole("member"); // 默认分配普通会员角色
        
        // constraint users_chk_2: account_status in ('pending', 'approved', 'suspended')
        user.setAccountStatus("approved"); // 默认账号状态为已批准
        
        // constraint users_chk_3: auth_provider in ('local', 'google', 'facebook')
        user.setAuthProvider("local"); // 默认本地注册
        
        // 记录创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        // 使用 MyBatis-Plus 的 save 方法保存到数据库
        this.save(user);

        return user;
    }
    @Override
    public User login(com.example.demo.dto.UserLoginDTO loginDTO) {
        // 1. 根据邮箱查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", loginDTO.getEmail());
        User user = this.getOne(queryWrapper);

        // 2. 校验用户是否存在
        if (user == null) {
            throw new RuntimeException("邮箱或密码错误！"); // 模糊提示，防止枚举邮箱
        }

        // 3. 校验密码 (使用 jbcrypt)
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("邮箱或密码错误！");
        }

        // 4. 返回查找到的用户信息
        return user;
    }
}
