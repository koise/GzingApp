-- Navigation History Table (Simplified Version)
-- This table stores successful navigation sessions for history tracking
-- No foreign key constraints for compatibility

CREATE TABLE IF NOT EXISTS `navigation_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `navigation_log_id` int(11) DEFAULT NULL COMMENT 'Reference to the original navigation log',
  `start_latitude` decimal(10,8) NOT NULL,
  `start_longitude` decimal(11,8) NOT NULL,
  `end_latitude` decimal(10,8) NOT NULL,
  `end_longitude` decimal(11,8) NOT NULL,
  `destination_name` varchar(255) NOT NULL,
  `destination_address` text,
  `route_distance` decimal(8,2) NOT NULL COMMENT 'Distance in kilometers',
  `estimated_duration` int(11) DEFAULT NULL COMMENT 'Estimated duration in minutes',
  `actual_duration` int(11) DEFAULT NULL COMMENT 'Actual duration in minutes',
  `estimated_fare` decimal(8,2) DEFAULT NULL COMMENT 'Estimated fare in pesos',
  `actual_fare` decimal(8,2) DEFAULT NULL COMMENT 'Actual fare paid in pesos',
  `transport_mode` enum('driving','walking','cycling','public_transport') NOT NULL DEFAULT 'driving',
  `success_rate` decimal(5,2) DEFAULT 100.00 COMMENT 'Success rate percentage',
  `completion_time` timestamp NULL DEFAULT NULL COMMENT 'When the navigation was completed',
  `start_time` timestamp NOT NULL COMMENT 'When navigation started',
  `end_time` timestamp NOT NULL COMMENT 'When navigation ended',
  `waypoints_count` int(11) DEFAULT 0 COMMENT 'Number of waypoints visited',
  `traffic_condition` varchar(50) DEFAULT NULL COMMENT 'Traffic condition during navigation',
  `average_speed` decimal(6,2) DEFAULT NULL COMMENT 'Average speed in km/h',
  `route_quality` enum('excellent','good','fair','poor') DEFAULT 'good' COMMENT 'Route quality rating',
  `user_rating` tinyint(1) DEFAULT NULL COMMENT 'User rating 1-5 stars',
  `notes` text COMMENT 'User notes about the navigation',
  `is_favorite` tinyint(1) DEFAULT 0 COMMENT 'Whether this route is marked as favorite',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_destination_name` (`destination_name`),
  KEY `idx_completion_time` (`completion_time`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_transport_mode` (`transport_mode`),
  KEY `idx_is_favorite` (`is_favorite`),
  KEY `idx_navigation_log_id` (`navigation_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better performance
CREATE INDEX `idx_navigation_history_composite` ON `navigation_history` (`user_id`, `completion_time` DESC);
CREATE INDEX `idx_navigation_history_destination` ON `navigation_history` (`user_id`, `destination_name`);

-- Create a view for navigation statistics
CREATE OR REPLACE VIEW `navigation_history_stats` AS
SELECT 
    user_id,
    COUNT(*) as total_navigations,
    COUNT(CASE WHEN completion_time IS NOT NULL THEN 1 END) as successful_navigations,
    ROUND(AVG(actual_duration), 2) as avg_duration_minutes,
    ROUND(AVG(route_distance), 2) as avg_distance_km,
    ROUND(AVG(average_speed), 2) as avg_speed_kmh,
    COUNT(CASE WHEN is_favorite = 1 THEN 1 END) as favorite_routes,
    MAX(completion_time) as last_navigation,
    MIN(start_time) as first_navigation
FROM navigation_history 
GROUP BY user_id;

-- Create a view for popular destinations
CREATE OR REPLACE VIEW `popular_destinations` AS
SELECT 
    user_id,
    destination_name,
    destination_address,
    COUNT(*) as visit_count,
    ROUND(AVG(actual_duration), 2) as avg_duration,
    ROUND(AVG(route_distance), 2) as avg_distance,
    MAX(completion_time) as last_visit,
    ROUND(AVG(user_rating), 2) as avg_rating
FROM navigation_history 
WHERE completion_time IS NOT NULL
GROUP BY user_id, destination_name, destination_address
HAVING visit_count > 0
ORDER BY visit_count DESC, last_visit DESC;

-- Insert sample data for testing
INSERT INTO `navigation_history` (
    `user_id`, `navigation_log_id`, `start_latitude`, `start_longitude`, 
    `end_latitude`, `end_longitude`, `destination_name`, `destination_address`,
    `route_distance`, `estimated_duration`, `actual_duration`, `estimated_fare`, `actual_fare`,
    `transport_mode`, `success_rate`, `completion_time`, `start_time`, `end_time`, `waypoints_count`,
    `traffic_condition`, `average_speed`, `route_quality`, `user_rating`, `is_favorite`
) VALUES 
(10, 1, 14.62270180, 121.17656790, 14.59758003, 121.17244053, 
 'Dela Paz Elementary School', 'Antipolo City', 3.27, 6, 8, 25.0, 30.0, 'driving',
 100.00, '2025-09-13 20:59:30', '2025-09-13 20:59:14', '2025-09-13 21:07:14', 5,
 'Moderate Traffic', 24.5, 'good', 4, 0),
 
(10, 2, 14.62272000, 121.17658790, 14.62231419, 121.17618992,
 'Langhaya', 'Antipolo City', 0.06, 1, 2, 15.0, 15.0, 'driving',
 100.00, '2025-09-13 21:00:01', '2025-09-13 21:00:01', '2025-09-13 21:02:01', 1,
 'Moderate Traffic', 1.8, 'excellent', 5, 1),
 
(10, 3, 14.62270180, 121.17656790, 14.60535531, 121.17412679,
 'Assumption Hospital', 'Antipolo City', 2.1, 4, 5, 20.0, 25.0, 'driving',
 100.00, '2025-09-13 21:15:00', '2025-09-13 21:10:00', '2025-09-13 21:15:00', 3,
 'Heavy Traffic', 25.2, 'good', 3, 0);
