-- Test SQL queries for navigation routes by user ID
-- Execute these queries in your database to check if data exists

-- 1. Check if navigation_routes table exists
SHOW TABLES LIKE 'navigation_routes';

-- 2. Check table structure
DESCRIBE navigation_routes;

-- 3. Count total routes in table
SELECT COUNT(*) as total_routes FROM navigation_routes;

-- 4. Count routes for user ID 32
SELECT COUNT(*) as routes_for_user_32 FROM navigation_routes WHERE user_id = 32;

-- 5. Get all routes for user ID 32
SELECT 
    id,
    user_id,
    route_name,
    destination_name,
    route_distance,
    estimated_duration,
    estimated_fare,
    transport_mode,
    is_favorite,
    usage_count,
    created_at,
    updated_at
FROM navigation_routes 
WHERE user_id = 32 
ORDER BY created_at DESC;

-- 6. Get all users and their route counts
SELECT 
    user_id,
    COUNT(*) as route_count,
    MIN(created_at) as first_route,
    MAX(created_at) as last_route
FROM navigation_routes 
GROUP BY user_id 
ORDER BY route_count DESC;

-- 7. Insert a test route for user ID 32 (if you want to add test data)
INSERT INTO navigation_routes (
    user_id,
    route_name,
    route_description,
    start_latitude,
    start_longitude,
    end_latitude,
    end_longitude,
    destination_name,
    destination_address,
    route_distance,
    estimated_duration,
    estimated_fare,
    transport_mode,
    route_quality,
    traffic_condition,
    average_speed,
    waypoints_count,
    is_favorite,
    is_public,
    usage_count,
    last_used
) VALUES (
    32,
    'Test Route for User 32',
    'This is a test route created for debugging',
    14.62270180,
    121.17656790,
    14.59758003,
    121.17244053,
    'Test Destination',
    'Test Address, Manila',
    3.27,
    15,
    15.00,
    'driving',
    'good',
    'Moderate Traffic',
    24.50,
    5,
    1,
    0,
    1,
    NOW()
);

-- 8. Verify the test route was inserted
SELECT * FROM navigation_routes WHERE user_id = 32 ORDER BY created_at DESC LIMIT 5;
