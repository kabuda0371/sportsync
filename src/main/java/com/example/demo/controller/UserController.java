package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

import com.example.demo.dto.GoogleLoginDTO;
import com.example.demo.dto.ProfileUpdateDTO;
import com.example.demo.dto.ResendVerificationDTO;
import com.example.demo.dto.UserRegisterDTO;
import com.example.demo.dto.UserLoginDTO;
import com.example.demo.dto.VerifyEmailDTO;
import com.example.demo.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.vo.UserVO;
import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户注册、登录及信息查询接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "根据邮箱、密码、姓名进行新用户注册，注册后需要验证邮箱")
    public Result<UserVO> registerUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return Result.success("注册成功，验证码已发送至您的邮箱，请查收", userVO);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "邮箱验证", description = "使用注册时发送的6位验证码完成邮箱验证")
    public Result<Void> verifyEmail(@Valid @RequestBody VerifyEmailDTO verifyEmailDTO) {
        userService.verifyEmail(verifyEmailDTO.getEmail(), verifyEmailDTO.getCode());
        return Result.success("邮箱验证成功，您现在可以登录了", null);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "重新发送验证码", description = "重新发送邮箱验证码，每次请求之间需要等待60秒")
    public Result<Void> resendVerification(@Valid @RequestBody ResendVerificationDTO resendDTO) {
        userService.resendVerificationCode(resendDTO.getEmail());
        return Result.success("验证码已重新发送，请查收邮箱", null);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用邮箱和密码进行登录，成功返回用户基本信息")
    public Result<UserVO> loginUser(@Valid @RequestBody UserLoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO);
        return Result.success("登录成功", userVO);
    }

    @PostMapping("/google-login")
    @Operation(summary = "Google 第三方登录", description = "使用 Google ID Token 进行第三方登录，首次登录会自动注册")
    public Result<UserVO> googleLogin(@Valid @RequestBody GoogleLoginDTO googleLoginDTO) {
        UserVO userVO = userService.googleLogin(googleLoginDTO);
        return Result.success("Google login successful", userVO);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "根据请求头中的 Token 获取当前登录用户信息")
    public Result<UserVO> getCurrentUser() {
        Long userId = UserContext.getUserId();
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success("获取用户信息成功", userVO);
    }

    @PutMapping("/me")
    @Operation(summary = "补全用户资料", description = "更新当前用户的生日和地址信息")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateDTO profileUpdateDTO) {
        Long userId = UserContext.getUserId();
        UserVO userVO = userService.updateProfile(userId, profileUpdateDTO);
        return Result.success("资料更新成功", userVO);
    }

    @DeleteMapping("/me")
    @Operation(summary = "注销账号", description = "会员注销自己的账号，账号状态将被设置为 suspended，注销后无法再登录")
    public Result<Void> deactivateAccount() {
        Long userId = UserContext.getUserId();
        userService.deactivateAccount(userId);
        return Result.success("账号已注销", null);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询用户列表（分页）", description = "管理员根据角色或账号状态分页筛选用户")
    public Result<IPage<UserVO>> listUsers(
            @Parameter(description = "页码，默认1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数，默认10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "角色过滤：member/staff/admin") @RequestParam(required = false) String role,
            @Parameter(description = "账号状态过滤：pending/approved/suspended") @RequestParam(required = false) String status) {
        IPage<UserVO> result = userService.listUsers(page, size, role, status);
        return Result.success("查询成功", result);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新用户状态", description = "管理员审批或改变用户的账号状态 (pending, approved, suspended)")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam String status) {
        userService.updateUserStatus(id, status);
        return Result.success("用户状态更新成功", null);
    }
}
