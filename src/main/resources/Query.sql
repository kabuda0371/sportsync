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

-- 体育设施信息表
CREATE TABLE facilities (
    -- 设施ID
    facility_id INT AUTO_INCREMENT PRIMARY KEY,
    -- 设施名称
    name VARCHAR(100) NOT NULL,
    -- 设施类型(例如：badminton courts, football pitches)
    type VARCHAR(50),
    -- 设施描述
    description TEXT,
    -- 使用指南
    usage_guidelines TEXT,
    -- 容量限制(最大容纳人数)
    capacity_limit INT,
    -- 每次预订的时间段限制(分钟)
    time_slot_limit_minutes INT,
    -- 关联到负责该设施的员工ID
    assigned_staff_id INT,
    -- 记录创建时间
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 记录更新时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);



-- 设施场地预订记录表
CREATE TABLE bookings (
    -- 预订记录ID
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    -- 发起预订的用户ID
    user_id INT NOT NULL,
    -- 被预订的设施ID
    facility_id INT NOT NULL,
    -- 预订日期
    booking_date DATE NOT NULL,
    -- 预订开始时间
    start_time TIME NOT NULL,
    -- 预订结束时间
    end_time TIME NOT NULL,
    -- 预订状态(待审批、已批准、已拒绝、已取消)
    status VARCHAR(20) DEFAULT 'PENDING' NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    -- 记录创建时间
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 记录更新时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入一些默认的体育设施数据
INSERT INTO facilities (name, type, description, usage_guidelines, capacity_limit, time_slot_limit_minutes, assigned_staff_id) VALUES
('Court A', 'badminton', 'Standard indoor badminton court with wooden flooring.', 'Please wear non-marking shoes. Clean up after use.', 4, 60, NULL),
('Court B', 'badminton', 'Standard indoor badminton court with synthetic flooring.', 'Please wear non-marking shoes. No food or drinks allowed on the court.', 4, 60, NULL),
('Main Pitch', 'football', 'Outdoor 11-a-side football pitch with artificial turf.', 'Studded boots permitted. Please do not leave trash on the field.', 22, 120, NULL),
('Swimming Pool 1', 'swimming', '50m Olympic size swimming pool.', 'Swimming cap required. Shower before entering the pool.', 50, 90, NULL),
('Tennis Court 1', 'tennis', 'Outdoor hard court for tennis.', 'Only tennis shoes allowed. Rackets and balls not provided.', 4, 60, NULL);

-- 插入一些默认的预订测试数据 (假设我们已经有一个 user_id=1 的会员)
-- 注意：运行这段 SQL 之前请确保 users 表中已经存在 user_id=1 的记录，否则会有外键约束或者逻辑错误
INSERT INTO bookings (user_id, facility_id, booking_date, start_time, end_time, status) VALUES
(1, 1, CURRENT_DATE, '10:00:00', '11:00:00', 'APPROVED'),
(1, 1, CURRENT_DATE + INTERVAL 1 DAY, '14:00:00', '15:00:00', 'PENDING'),
(1, 3, CURRENT_DATE + INTERVAL 2 DAY, '18:00:00', '20:00:00', 'APPROVED');

show tables;
show create table users;
select * from users;