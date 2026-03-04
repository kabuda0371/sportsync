show databases;
create database sports_centre_db;
use sports_centre_db;
CREATE TABLE users (
    -- 1. 基础身份与认证字段
                       user_id INT AUTO_INCREMENT PRIMARY KEY,

    -- 角色区分：会员、员工、管理员
                       role VARCHAR(20) DEFAULT 'member' NOT NULL
                           CHECK (role IN ('member', 'staff', 'admin')),

    -- 登录邮箱，必须唯一
                       email VARCHAR(255) UNIQUE NOT NULL,

    -- 密码哈希值。因为高级需求要求支持 Google/Facebook 登录，第三方登录时没有本地密码，所以允许为 NULL
                       password_hash VARCHAR(255),

    -- 2. 个人基本信息 (注册要求)
                       name VARCHAR(100) NOT NULL,

    -- 出生日期和地址：作业要求会员注册时提供，但员工/管理员可能不需要，所以数据库层面可以允许为 NULL，由后端代码控制会员必填
                       date_of_birth DATE,
                       address TEXT,

    -- 3. 员工/账号管理字段
    -- 满足管理员可以“批准或暂停员工账户”的需求
                       account_status VARCHAR(20) DEFAULT 'approved' NOT NULL
                           CHECK (account_status IN ('pending', 'approved', 'suspended')),

    -- 4. “运动搭子”档案字段 (仅限会员，且为选填)
    -- 会员可以选择加入(opt in)搭子匹配功能
                       is_partner_matching_enabled BOOLEAN DEFAULT FALSE,
                       preferred_sport VARCHAR(100), -- 偏好运动，如 'badminton'
                       skill_level VARCHAR(50),      -- 技能水平
                       availability VARCHAR(255),    -- 空闲时间描述

    -- 5. 高级需求：第三方登录 (Social Login)
                       auth_provider VARCHAR(50) DEFAULT 'local' NOT NULL
                           CHECK (auth_provider IN ('local', 'google', 'facebook')),
                       social_id VARCHAR(255) UNIQUE, -- 存储 Google/Facebook 返回的唯一用户 ID

    -- 6. 时间戳
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    -- 1. 基础身份与认证字段
                       user_id INT AUTO_INCREMENT PRIMARY KEY,

    -- 角色区分：会员、员工、管理员
                       role VARCHAR(20) DEFAULT 'member' NOT NULL
                           CHECK (role IN ('member', 'staff', 'admin')),

    -- 登录邮箱，必须唯一
                       email VARCHAR(255) UNIQUE NOT NULL,

    -- 密码哈希值。因为高级需求要求支持 Google/Facebook 登录，第三方登录时没有本地密码，所以允许为 NULL
                       password_hash VARCHAR(255),

    -- 2. 个人基本信息 (注册要求)
                       name VARCHAR(100) NOT NULL,

    -- 出生日期和地址：作业要求会员注册时提供，但员工/管理员可能不需要，所以数据库层面可以允许为 NULL，由后端代码控制会员必填
                       date_of_birth DATE,
                       address TEXT,

    -- 3. 员工/账号管理字段
    -- 满足管理员可以“批准或暂停员工账户”的需求
                       account_status VARCHAR(20) DEFAULT 'approved' NOT NULL
                           CHECK (account_status IN ('pending', 'approved', 'suspended')),

    -- 4. “运动搭子”档案字段 (仅限会员，且为选填)
    -- 会员可以选择加入(opt in)搭子匹配功能
                       is_partner_matching_enabled BOOLEAN DEFAULT FALSE,
                       preferred_sport VARCHAR(100), -- 偏好运动，如 'badminton'
                       skill_level VARCHAR(50),      -- 技能水平
                       availability VARCHAR(255),    -- 空闲时间描述

    -- 5. 高级需求：第三方登录 (Social Login)
                       auth_provider VARCHAR(50) DEFAULT 'local' NOT NULL
                           CHECK (auth_provider IN ('local', 'google', 'facebook')),
                       social_id VARCHAR(255) UNIQUE, -- 存储 Google/Facebook 返回的唯一用户 ID

    -- 6. 时间戳
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

show create table users;
select * from users;