-- Navigation Routes Table
-- This table stores saved navigation routes for future reference
-- Created for GzingApp mobile application

CREATE TABLE IF NOT EXISTS `navigation_routes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `route_name` varchar(255) NOT NULL COMMENT 'User-defined name for the route',
  `route_description` text COMMENT 'Optional description of the route',
  `start_latitude` decimal(10,8) NOT NULL,
  `start_longitude` decimal(11,8) NOT NULL,
  `end_latitude` decimal(10,8) NOT NULL,
  `end_longitude` decimal(11,8) NOT NULL,
  `destination_name` varchar(255) NOT NULL,
  `destination_address` text,
  `route_distance` decimal(8,2) NOT NULL COMMENT 'Distance in kilometers',
  `estimated_duration` int(11) DEFAULT NULL COMMENT 'Estimated duration in minutes',
  `estimated_fare` decimal(8,2) DEFAULT NULL COMMENT 'Estimated fare in local currency',
  `transport_mode` enum('driving','walking','cycling','public_transport') NOT NULL DEFAULT 'driving',
  `route_quality` enum('excellent','good','fair','poor') DEFAULT 'good' COMMENT 'Route quality rating',
  `traffic_condition` varchar(50) DEFAULT NULL COMMENT 'Typical traffic condition',
  `average_speed` decimal(6,2) DEFAULT NULL COMMENT 'Average speed in km/h',
  `waypoints_count` int(11) DEFAULT 0 COMMENT 'Number of waypoints in the route',
  `route_coordinates` longtext COMMENT 'JSON array of route coordinates for map display',
  `is_favorite` tinyint(1) DEFAULT 0 COMMENT 'Whether this route is marked as favorite',
  `is_public` tinyint(1) DEFAULT 0 COMMENT 'Whether this route is shared publicly',
  `usage_count` int(11) DEFAULT 0 COMMENT 'Number of times this route has been used',
  `last_used` timestamp NULL DEFAULT NULL COMMENT 'Last time this route was used',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_route_name` (`route_name`),
  KEY `idx_destination_name` (`destination_name`),
  KEY `idx_transport_mode` (`transport_mode`),
  KEY `idx_is_favorite` (`is_favorite`),
  KEY `idx_is_public` (`is_public`),
  KEY `idx_usage_count` (`usage_count`),
  KEY `idx_last_used` (`last_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create composite indexes for better performance
CREATE INDEX `idx_navigation_routes_user_favorite` ON `navigation_routes` (`user_id`, `is_favorite` DESC, `created_at` DESC);
CREATE INDEX `idx_navigation_routes_user_usage` ON `navigation_routes` (`user_id`, `usage_count` DESC, `last_used` DESC);
CREATE INDEX `idx_navigation_routes_public` ON `navigation_routes` (`is_public`, `usage_count` DESC, `created_at` DESC);

-- Create a view for route statistics
CREATE OR REPLACE VIEW `navigation_routes_stats` AS
SELECT 
    user_id,
    COUNT(*) as total_routes,
    COUNT(CASE WHEN is_favorite = 1 THEN 1 END) as favorite_routes,
    COUNT(CASE WHEN is_public = 1 THEN 1 END) as public_routes,
    ROUND(AVG(route_distance), 2) as avg_route_distance_km,
    ROUND(AVG(estimated_duration), 2) as avg_estimated_duration_min,
    ROUND(AVG(estimated_fare), 2) as avg_estimated_fare,
    ROUND(AVG(average_speed), 2) as avg_speed_kmh,
    SUM(usage_count) as total_usage_count,
    MAX(last_used) as last_route_used,
    MIN(created_at) as first_route_created
FROM navigation_routes 
GROUP BY user_id;

-- Create a view for popular public routes
CREATE OR REPLACE VIEW `popular_public_routes` AS
SELECT 
    id,
    user_id,
    route_name,
    route_description,
    destination_name,
    destination_address,
    route_distance,
    estimated_duration,
    estimated_fare,
    transport_mode,
    route_quality,
    traffic_condition,
    average_speed,
    usage_count,
    last_used,
    created_at
FROM navigation_routes 
WHERE is_public = 1
ORDER BY usage_count DESC, created_at DESC;

-- Create a view for user's most used routes
CREATE OR REPLACE VIEW `user_most_used_routes` AS
SELECT 
    user_id,
    id,
    route_name,
    destination_name,
    route_distance,
    transport_mode,
    usage_count,
    last_used,
    is_favorite
FROM navigation_routes 
WHERE usage_count > 0
ORDER BY user_id, usage_count DESC, last_used DESC;

-- Insert sample data (optional - for testing)
INSERT INTO `navigation_routes` (
    `user_id`, `route_name`, `route_description`, `start_latitude`, `start_longitude`, 
    `end_latitude`, `end_longitude`, `destination_name`, `destination_address`,
    `route_distance`, `estimated_duration`, `transport_mode`, `route_quality`,
    `traffic_condition`, `average_speed`, `waypoints_count`, `is_favorite`, 
    `is_public`, `usage_count`, `last_used`
) VALUES 
(10, 'Home to Work', 'Daily commute route to office', 14.62270180, 121.17656790, 
 14.59758003, 121.17244053, 'Office Building', 'Makati City', 3.27, 15, 'driving',
 'good', 'Moderate Traffic', 24.5, 5, 1, 0, 25, '2025-01-15 08:30:00'),

(10, 'Weekend Shopping', 'Route to favorite shopping mall', 14.62272000, 121.17658790, 
 14.62231419, 121.17618992, 'SM Mall', 'Antipolo City', 0.06, 2, 'driving',
 'excellent', 'Light Traffic', 18.0, 1, 0, 0, 8, '2025-01-14 14:20:00'),

(10, 'Gym Route', 'Quick route to fitness center', 14.62270180, 121.17656790, 
 14.60535531, 121.17412679, 'Fitness First', 'Antipolo City', 2.1, 8, 'driving',
 'good', 'Heavy Traffic', 15.8, 3, 1, 1, 12, '2025-01-13 18:45:00'),

(10, 'Walking to Park', 'Leisurely walk to nearby park', 14.62270180, 121.17656790, 
 14.62300000, 121.17700000, 'Rizal Park', 'Antipolo City', 0.5, 10, 'walking',
 'excellent', 'No Traffic', 3.0, 0, 0, 0, 5, '2025-01-12 16:30:00');

-- Create stored procedure to increment route usage
DELIMITER //
CREATE PROCEDURE IncrementRouteUsage(IN route_id INT)
BEGIN
    UPDATE navigation_routes 
    SET usage_count = usage_count + 1, 
        last_used = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = route_id;
END //
DELIMITER ;

-- Create stored procedure to get user's favorite routes
DELIMITER //
CREATE PROCEDURE GetUserFavoriteRoutes(IN user_id_param INT)
BEGIN
    SELECT * FROM navigation_routes 
    WHERE user_id = user_id_param AND is_favorite = 1
    ORDER BY created_at DESC;
END //
DELIMITER ;

-- Create stored procedure to search routes by destination
DELIMITER //
CREATE PROCEDURE SearchRoutesByDestination(
    IN user_id_param INT, 
    IN destination_search VARCHAR(255)
)
BEGIN
    SELECT * FROM navigation_routes 
    WHERE user_id = user_id_param 
    AND (destination_name LIKE CONCAT('%', destination_search, '%') 
         OR destination_address LIKE CONCAT('%', destination_search, '%'))
    ORDER BY usage_count DESC, created_at DESC;
END //
DELIMITER ;

-- Add comments to table and columns for documentation
ALTER TABLE `navigation_routes` 
COMMENT = 'Stores saved navigation routes for future reference and reuse';

ALTER TABLE `navigation_routes` 
MODIFY COLUMN `route_coordinates` longtext COMMENT 'JSON array of route coordinates: [{"lat": 14.62270180, "lng": 121.17656790}, ...]';

-- Create trigger to update last_used when usage_count changes
DELIMITER //
CREATE TRIGGER update_last_used_on_usage 
BEFORE UPDATE ON navigation_routes
FOR EACH ROW
BEGIN
    IF NEW.usage_count > OLD.usage_count THEN
        SET NEW.last_used = CURRENT_TIMESTAMP;
    END IF;
END //
DELIMITER ;
