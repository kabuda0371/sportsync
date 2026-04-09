package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Google 第三方登录请求对象")
public class GoogleLoginDTO {

    @NotBlank(message = "Google ID Token cannot be empty")
    @Schema(description = "Google 登录返回的 ID Token", example = "eyJhbGciOiJSUzI1NiIs...")
    private String idToken;
}
