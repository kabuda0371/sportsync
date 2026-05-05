CREATE DATABASE IF NOT EXISTS sports_centre_db;
USE sports_centre_db;

-- Users
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(20) NOT NULL DEFAULT 'member'
        CHECK (role IN ('member', 'staff', 'admin')),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    address TEXT,
    account_status VARCHAR(20) NOT NULL DEFAULT 'approved'
        CHECK (account_status IN ('pending', 'approved', 'suspended')),
    is_partner_matching_enabled BOOLEAN DEFAULT FALSE,
    preferred_sport VARCHAR(100) COMMENT 'Preferred sports, comma-separated, e.g. badminton,tennis',
    skill_level VARCHAR(50)
        CHECK (skill_level IN ('beginner', 'intermediate', 'advanced')),
    availability VARCHAR(255) COMMENT 'Available time slots, comma-separated, e.g. weekday_evening,weekend_morning',
    partner_bio VARCHAR(500) NULL COMMENT 'Short bio for partner matching',
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'local'
        CHECK (auth_provider IN ('local', 'google', 'facebook')),
    social_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Facilities
CREATE TABLE facilities (
    facility_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    description TEXT,
    usage_guidelines TEXT,
    capacity_limit INT,
    time_slot_limit_minutes INT,
    assigned_staff_id INT,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Bookings
CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    facility_id INT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'awaiting_partner', 'approved', 'rejected', 'cancelled', 'completed')),
    activity_description TEXT NULL COMMENT 'Member provided activity details',
    staff_note TEXT NULL COMMENT 'Staff review note',
    suggested_facility_id INT NULL COMMENT 'Suggested replacement facility ID',
    partner_ids VARCHAR(255) NULL COMMENT 'Shared booking partner user IDs, comma-separated',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Notifications
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(64) NOT NULL DEFAULT 'GENERAL',
    related_id BIGINT NULL,
    booking_id INT NULL,
    message VARCHAR(500) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- Partner matching requests
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
    UNIQUE KEY uq_requester_target (requester_id, target_id)
);

-- Equipment reports
CREATE TABLE equipment_reports (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    facility_id INT NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'noted'
        CHECK (status IN ('noted', 'repair_in_progress', 'resolved')),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (facility_id) REFERENCES facilities(facility_id) ON DELETE CASCADE
);

-- Booking invitations
CREATE TABLE booking_invitations (
    invitation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    invitee_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'accepted', 'declined')),
    responded_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (invitee_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uq_booking_invitee (booking_id, invitee_id)
);

-- Existing database upgrade
-- If your database was created before shared booking support was added,
-- run the statements below once to align bookings_chk_1 with the current code.
--
-- ALTER TABLE bookings
--     DROP CHECK bookings_chk_1;
--
-- ALTER TABLE bookings
--     ADD CONSTRAINT bookings_chk_1
--         CHECK (status IN (
--             'pending',
--             'awaiting_partner',
--             'approved',
--             'rejected',
--             'cancelled',
--             'completed'
--         ));


INSERT INTO facilities (
    name,
    type,
    description,
    usage_guidelines,
    capacity_limit,
    time_slot_limit_minutes,
    latitude,
    longitude
) VALUES
    ('Main Badminton Court', 'Badminton', 'Indoor standard badminton court with professional lighting.', 'Soft-soled shoes required. Wipe equipment after use.', 4, 60, 51.5080, -0.1270),
    ('Tennis Court A', 'Tennis', 'Outdoor hard-surface tennis court.', 'Bring your own racket. No food on court.', 4, 60, 51.5075, -0.1265),
    ('Swimming Pool', 'Swimming', 'Olympic-size indoor swimming pool, 8 lanes.', 'Swim cap required. No diving in shallow end.', 20, 60, 51.5068, -0.1280),
    ('Gym Hall', 'Gym', 'Fully equipped gym with cardio and weight machines.', 'Wipe machines after use. Closed-toe shoes only.', 30, 60, 51.5085, -0.1275),
    ('Football Pitch', 'Football', 'Full-size outdoor football pitch with natural grass.', 'Football boots only. No bikes on pitch.', 22, 90, 51.5072, -0.1260);
