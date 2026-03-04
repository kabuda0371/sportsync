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
import com.example.demo.vo.UserVO;
import org.springframework.beans.BeanUtils;

import com.example.demo.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户注册、登录及信息查询接口")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @Operation(summary = "获取所有用户列表", description = "返回系统中所有的用户信息。仅供测试或管理员使用。")
    public Result<List<User>> listAllUsers() {
        return Result.success(userMapper.selectList(null));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "根据邮箱、密码、姓名进行新用户注册")
    public Result<UserVO> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        System.out.println(111);
        try {
            System.out.println(registerDTO);
            User savedUser = userService.register(registerDTO);

            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(savedUser, userVO);
            
            return Result.success("用户注册成功", userVO);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用邮箱和密码进行登录，成功返回用户基本信息")
    public Result<UserVO> loginUser(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            User user = userService.login(loginDTO);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return Result.success("登录成功", userVO);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
