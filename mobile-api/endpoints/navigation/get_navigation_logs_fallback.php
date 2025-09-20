<?php
/**
 * Fallback Navigation Logs Endpoint
 * Returns sample navigation logs data without database connection
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
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;
    
    // Sample navigation logs data
    $sampleLogs = [
        [
            'id' => 1,
            'user_id' => 10,
            'activity_type' => 'navigation_start',
            'start_latitude' => 14.5995,
            'start_longitude' => 120.9842,
            'destination_name' => 'Luneta Park',
            'destination_address' => 'Luneta Park, Manila',
            'route_distance' => 12.5,
            'estimated_duration' => 25,
            'transport_mode' => 'driving',
            'status' => 'completed',
            'destination_reached' => true,
            'created_at' => '2024-01-15 10:30:00',
            'updated_at' => '2024-01-15 10:55:00'
        ],
        [
            'id' => 2,
            'user_id' => 10,
            'activity_type' => 'navigation_start',
            'start_latitude' => 14.6500,
            'start_longitude' => 121.0700,
            'destination_name' => 'La Salle Greenhills',
            'destination_address' => 'La Salle Greenhills, San Juan',
            'route_distance' => 8.3,
            'estimated_duration' => 18,
            'transport_mode' => 'driving',
            'status' => 'completed',
            'destination_reached' => true,
            'created_at' => '2024-01-14 14:20:00',
            'updated_at' => '2024-01-14 14:38:00'
        ],
        [
            'id' => 3,
            'user_id' => 10,
            'activity_type' => 'navigation_start',
            'start_latitude' => 14.5500,
            'start_longitude' => 121.0000,
            'destination_name' => 'Cubao',
            'destination_address' => 'Cubao, Quezon City',
            'route_distance' => 15.2,
            'estimated_duration' => 35,
            'transport_mode' => 'driving',
            'status' => 'in_progress',
            'destination_reached' => false,
            'created_at' => '2024-01-13 09:15:00',
            'updated_at' => '2024-01-13 09:15:00'
        ]
    ];
    
    // Filter by user_id if provided
    if ($userId !== null) {
        $sampleLogs = array_filter($sampleLogs, function($log) use ($userId) {
            return $log['user_id'] === $userId;
        });
    }
    
    // Convert back to indexed array
    $sampleLogs = array_values($sampleLogs);
    
    // Apply pagination
    $total = count($sampleLogs);
    $offset = ($page - 1) * $limit;
    $paginatedLogs = array_slice($sampleLogs, $offset, $limit);
    
    // Calculate pagination info
    $totalPages = ceil($total / $limit);
    
    // Return success response
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Navigation logs retrieved successfully (fallback data)',
        'data' => [
            'logs' => $paginatedLogs,
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
        'message' => 'Failed to retrieve navigation logs: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>

