package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long id;
    
    private String role;
    
    private String email;
    
    private String passwordHash;
    
    private String name;
    
    private LocalDate dateOfBirth;
    
    private String address;
    
    private String accountStatus;
    
    @TableField("is_partner_matching_enabled")
    private Boolean partnerMatchingEnabled;
    
    private String preferredSport;
    
    private String skillLevel;
    
    private String availability;
    
    private String authProvider;
    
    private String socialId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

}
