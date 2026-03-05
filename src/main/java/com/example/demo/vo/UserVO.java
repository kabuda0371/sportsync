package com.example.demo.vo;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户信息响应对象")
public class UserVO {
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;
    
    @Schema(description = "姓名", example = "张三")
    private String name;
    
    @Schema(description = "角色", example = "member")
    private String role;
    
    @Schema(description = "账号状态", example = "approved")
    private String accountStatus;

    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
}
