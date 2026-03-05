package com.example.demo.controller;

import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserVO;
import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户注册、登录及信息查询接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "根据邮箱、密码、姓名进行新用户注册")
    public Result<UserVO> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return Result.success("用户注册成功", userVO);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用邮箱和密码进行登录，成功返回用户基本信息")
    public Result<UserVO> loginUser(@Valid @RequestBody UserLoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO);
        return Result.success("登录成功", userVO);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "根据请求头中的 Token 获取当前登录用户信息")
    public Result<UserVO> getCurrentUser() {
        Long userId = UserContext.getUserId();
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success("获取用户信息成功", userVO);
    }
}
