-- Emergency SMS Logs Table
-- This table stores logs of emergency SMS attempts for tracking and analytics

CREATE TABLE IF NOT EXISTS `emergency_sms_logs` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` int(11) NOT NULL,
    `latitude` decimal(10,8) NOT NULL,
    `longitude` decimal(11,8) NOT NULL,
    `emergency_type` varchar(50) DEFAULT 'emergency',
    `contacts_json` text NOT NULL COMMENT 'JSON array of phone numbers',
    `success_count` int(11) DEFAULT 0,
    `failure_count` int(11) DEFAULT 0,
    `message_content` text COMMENT 'The actual SMS message sent',
    `location_address` text COMMENT 'Reverse geocoded address',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_emergency_type` (`emergency_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample data (optional)
INSERT INTO `emergency_sms_logs` (
    `user_id`, 
    `latitude`, 
    `longitude`, 
    `emergency_type`, 
    `contacts_json`, 
    `success_count`, 
    `failure_count`,
    `message_content`,
    `location_address`
) VALUES 
(
    10, 
    14.62270180, 
    121.17656790, 
    'emergency', 
    '["639123456789", "639987654321"]', 
    2, 
    0,
    '🚨 EMERGENCY ALERT 🚨\n\n📍 Location Details:\nAddress: Antipolo City, Rizal, Philippines\nCoordinates: 14.62270180, 121.17656790\nGoogle Maps: https://maps.google.com/maps?q=14.62270180,121.17656790\n\n⏰ Time: 2025-01-14 10:30:00\n🚨 Emergency Type: Emergency\n\nPlease help immediately! This is an automated emergency message from GzingApp.',
    'Antipolo City, Rizal, Philippines'
),
(
    10, 
    14.5995, 
    120.9842, 
    'medical', 
    '["639123456789"]', 
    1, 
    0,
    '🚨 EMERGENCY ALERT 🚨\n\nMessage: Medical emergency, need immediate help\n\n📍 Location Details:\nAddress: Manila, Metro Manila, Philippines\nCoordinates: 14.5995, 120.9842\nGoogle Maps: https://maps.google.com/maps?q=14.5995,120.9842\n\n⏰ Time: 2025-01-14 11:15:00\n🚨 Emergency Type: Medical\n\nPlease help immediately! This is an automated emergency message from GzingApp.',
    'Manila, Metro Manila, Philippines'
);

