<?php
/**
 * Fallback Routes Endpoint
 * Returns sample routes data without database connection
 */

// Set content type and CORS headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

try {
    // Get query parameters
    $page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
    $limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 20;
    $search = isset($_GET['search']) ? trim($_GET['search']) : '';
    $status = isset($_GET['status']) ? trim($_GET['status']) : 'active';
    
    // Sample routes data
    $sampleRoutes = [
        [
            'id' => 1,
            'name' => 'Downtown Express',
            'description' => 'Fast route through downtown area with major landmarks',
            'pincount' => 5,
            'kilometer' => '12.5',
            'estimated_total_fare' => '45.00',
            'map_details' => [
                'pins' => [
                    ['lat' => 14.5995, 'lng' => 120.9842, 'name' => 'Start Point', 'address' => 'Manila City Hall'],
                    ['lat' => 14.6042, 'lng' => 120.9822, 'name' => 'Waypoint 1', 'address' => 'Intramuros'],
                    ['lat' => 14.6091, 'lng' => 120.9789, 'name' => 'Waypoint 2', 'address' => 'Rizal Park'],
                    ['lat' => 14.6145, 'lng' => 120.9756, 'name' => 'Waypoint 3', 'address' => 'National Museum'],
                    ['lat' => 14.6200, 'lng' => 120.9720, 'name' => 'Destination', 'address' => 'Luneta Park']
                ],
                'route_type' => 'express',
                'difficulty' => 'easy'
            ],
            'status' => 'active',
            'created_at' => '2024-01-01 00:00:00',
            'updated_at' => '2024-01-01 00:00:00'
        ],
        [
            'id' => 2,
            'name' => 'University Loop',
            'description' => 'Scenic route connecting major universities in the area',
            'pincount' => 4,
            'kilometer' => '8.3',
            'estimated_total_fare' => '32.50',
            'map_details' => [
                'pins' => [
                    ['lat' => 14.6500, 'lng' => 121.0700, 'name' => 'Start Point', 'address' => 'UP Diliman'],
                    ['lat' => 14.6550, 'lng' => 121.0750, 'name' => 'Waypoint 1', 'address' => 'Ateneo de Manila'],
                    ['lat' => 14.6600, 'lng' => 121.0800, 'name' => 'Waypoint 2', 'address' => 'Miriam College'],
                    ['lat' => 14.6650, 'lng' => 121.0850, 'name' => 'Destination', 'address' => 'La Salle Greenhills']
                ],
                'route_type' => 'scenic',
                'difficulty' => 'moderate'
            ],
            'status' => 'active',
            'created_at' => '2024-01-01 00:00:00',
            'updated_at' => '2024-01-01 00:00:00'
        ],
        [
            'id' => 3,
            'name' => 'Business District',
            'description' => 'Route through major business and commercial areas',
            'pincount' => 6,
            'kilometer' => '15.2',
            'estimated_total_fare' => '58.00',
            'map_details' => [
                'pins' => [
                    ['lat' => 14.5500, 'lng' => 121.0000, 'name' => 'Start Point', 'address' => 'Makati CBD'],
                    ['lat' => 14.5550, 'lng' => 121.0050, 'name' => 'Waypoint 1', 'address' => 'BGC Taguig'],
                    ['lat' => 14.5600, 'lng' => 121.0100, 'name' => 'Waypoint 2', 'address' => 'Ortigas Center'],
                    ['lat' => 14.5650, 'lng' => 121.0150, 'name' => 'Waypoint 3', 'address' => 'Eastwood City'],
                    ['lat' => 14.5700, 'lng' => 121.0200, 'name' => 'Waypoint 4', 'address' => 'Quezon City Circle'],
                    ['lat' => 14.5750, 'lng' => 121.0250, 'name' => 'Destination', 'address' => 'Cubao']
                ],
                'route_type' => 'business',
                'difficulty' => 'hard'
            ],
            'status' => 'active',
            'created_at' => '2024-01-01 00:00:00',
            'updated_at' => '2024-01-01 00:00:00'
        ]
    ];
    
    // Filter routes by status
    $filteredRoutes = array_filter($sampleRoutes, function($route) use ($status) {
        return $route['status'] === $status;
    });
    
    // Filter by search if provided
    if (!empty($search)) {
        $filteredRoutes = array_filter($filteredRoutes, function($route) use ($search) {
            return stripos($route['name'], $search) !== false || 
                   stripos($route['description'], $search) !== false;
        });
    }
    
    // Convert back to indexed array
    $filteredRoutes = array_values($filteredRoutes);
    
    // Apply pagination
    $total = count($filteredRoutes);
    $offset = ($page - 1) * $limit;
    $paginatedRoutes = array_slice($filteredRoutes, $offset, $limit);
    
    // Calculate pagination info
    $totalPages = ceil($total / $limit);
    
    // Return success response
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Routes retrieved successfully (fallback data)',
        'data' => [
            'routes' => $paginatedRoutes,
            'pagination' => [
                'current_page' => $page,
                'total_pages' => $totalPages,
                'total_items' => $total,
                'items_per_page' => $limit,
                'has_next' => $page < $totalPages,
                'has_prev' => $page > 1
            ]
        ],
        'timestamp' => date('Y-m-d H:i:s'),
        'note' => 'This is fallback data. Database connection is not available.'
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Failed to retrieve routes: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>

