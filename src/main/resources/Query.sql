CREATE DATABASE IF NOT EXISTS sports_centre_db;
USE sports_centre_db;

-- 用户表
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,

    -- 角色区分：会员、员工、管理员
    role VARCHAR(20) DEFAULT 'member' NOT NULL
        CHECK (role IN ('member', 'staff', 'admin')),

    -- 登录邮箱，必须唯一
    email VARCHAR(255) UNIQUE NOT NULL,

    -- 密码哈希值，第三方登录时允许为 NULL
    password_hash VARCHAR(255),

    -- 个人基本信息
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    address TEXT,

    -- 账号状态
    account_status VARCHAR(20) DEFAULT 'approved' NOT NULL
        CHECK (account_status IN ('pending', 'approved', 'suspended')),

    -- 伙伴匹配档案字段（仅限会员，选填）
    is_partner_matching_enabled BOOLEAN DEFAULT FALSE,
    preferred_sport VARCHAR(100) COMMENT '偏好运动，逗号分隔，如 badminton,tennis',
    skill_level VARCHAR(50)
        CHECK (skill_level IN ('beginner', 'intermediate', 'advanced')),
    availability VARCHAR(255) COMMENT '可用时间，逗号分隔，如 weekday_evening,weekend_morning',
    partner_bio VARCHAR(500) NULL COMMENT '伙伴匹配活动简介',

    -- 第三方登录
    auth_provider VARCHAR(50) DEFAULT 'local' NOT NULL
        CHECK (auth_provider IN ('local', 'google', 'facebook')),
    social_id VARCHAR(255) UNIQUE,

    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 体育设施表
CREATE TABLE facilities (
    facility_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    description TEXT,
    usage_guidelines TEXT,
    capacity_limit INT,
    time_slot_limit_minutes INT,
    assigned_staff_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 预订表
CREATE TABLE bookings (
                          booking_id INT AUTO_INCREMENT PRIMARY KEY,
                          user_id INT NOT NULL,
                          facility_id INT NOT NULL,
                          booking_date DATE NOT NULL,
                          start_time TIME NOT NULL,
                          end_time TIME NOT NULL,
                          status VARCHAR(20) DEFAULT 'pending' NOT NULL
                              CHECK (status IN ('pending', 'approved', 'rejected', 'cancelled', 'completed')),
                          activity_description TEXT NULL COMMENT '会员预期活动描述',
                          staff_note TEXT NULL COMMENT '工作人员审批备注',
                          suggested_facility_id INT NULL COMMENT '建议替代设施ID',
                          partner_ids VARCHAR(255) NULL COMMENT '共享预订伙伴用户ID列表，逗号分隔',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 站内通知表
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    booking_id INT NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- 伙伴配对请求表
CREATE TABLE partner_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id INT NOT NULL,
    target_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'accepted', 'rejected')),
    message VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (target_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_requester_target (requester_id, target_id)
);

SET FOREIGN_KEY_CHECKS = 0;
drop table bookings;
SET FOREIGN_KEY_CHECKS = 1;


ALTER TABLE bookings ADD COLUMN partner_ids VARCHAR(255) NULL;
UPDATE bookings SET partner_ids = CAST(partner_id AS CHAR) WHERE partner_id IS NOT NULL;
ALTER TABLE bookings DROP FOREIGN KEY bookings_ibfk_N; -- 先删外键
ALTER TABLE bookings DROP COLUMN partner_id;

-- 设备报修表
CREATE TABLE equipment_reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id   BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'noted'
        CHECK (status IN ('noted', 'repair_in_progress', 'resolved')),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (facility_id) REFERENCES facilities(facility_id) ON DELETE CASCADE
);