<?php
// Navigation Routes API Index
// This file provides information about available navigation routes endpoints

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$endpoints = [
    'create_route' => [
        'method' => 'POST',
        'url' => '/mobile-api/endpoints/navigation-routes/create_navigation_route.php',
        'description' => 'Create a new navigation route',
        'required_fields' => [
            'user_id', 'route_name', 'start_latitude', 'start_longitude',
            'end_latitude', 'end_longitude', 'destination_name', 'route_distance', 'transport_mode'
        ],
        'optional_fields' => [
            'route_description', 'estimated_duration', 'route_quality', 'traffic_condition',
            'average_speed', 'waypoints_count', 'route_coordinates', 'is_favorite', 'is_public'
        ]
    ],
    'get_routes' => [
        'method' => 'GET',
        'url' => '/mobile-api/endpoints/navigation-routes/get_navigation_routes.php',
        'description' => 'Get user navigation routes with pagination and filtering',
        'query_parameters' => [
            'user_id' => 'User ID (required)',
            'limit' => 'Number of records to return (default: 50, max: 100)',
            'offset' => 'Number of records to skip (default: 0)',
            'order_by' => 'Field to order by (created_at, updated_at, route_name, destination_name, usage_count, last_used)',
            'order_direction' => 'ASC or DESC (default: DESC)',
            'favorites_only' => 'Show only favorite routes (true/false)',
            'search' => 'Search in route name, destination name, or address'
        ]
    ]
];

echo json_encode([
    'success' => true,
    'message' => 'Navigation Routes API Endpoints',
    'data' => [
        'api_version' => '1.0',
        'description' => 'API for managing saved navigation routes',
        'endpoints' => $endpoints,
        'usage_examples' => [
            'create_route' => [
                'url' => '/mobile-api/endpoints/navigation-routes/create_navigation_route.php',
                'method' => 'POST',
                'body' => [
                    'user_id' => 10,
                    'route_name' => 'Home to Work',
                    'route_description' => 'Daily commute route',
                    'start_latitude' => 14.62270180,
                    'start_longitude' => 121.17656790,
                    'end_latitude' => 14.59758003,
                    'end_longitude' => 121.17244053,
                    'destination_name' => 'Office Building',
                    'destination_address' => 'Makati City',
                    'route_distance' => 3.27,
                    'estimated_duration' => 15,
                    'transport_mode' => 'driving',
                    'route_quality' => 'good',
                    'is_favorite' => 1
                ]
            ],
            'get_routes' => [
                'url' => '/mobile-api/endpoints/navigation-routes/get_navigation_routes.php?user_id=10&limit=20&favorites_only=true',
                'method' => 'GET'
            ]
        ]
    ],
    'timestamp' => date('Y-m-d H:i:s')
]);
?>
