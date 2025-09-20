<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Only allow GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed. Only GET requests are supported.',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    exit();
}

try {
    // Include database configuration
    @include_once __DIR__ . '/../../config/database.php';
    
    // Get user_id from query parameter
    $user_id = isset($_GET['user_id']) ? (int)$_GET['user_id'] : null;
    
    if (!$user_id || $user_id <= 0) {
        throw new Exception('Valid user_id is required');
    }
    
    // Get optional parameters
    $limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 50;
    $offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0;
    $order_by = isset($_GET['order_by']) ? $_GET['order_by'] : 'completion_time';
    $order_direction = isset($_GET['order_direction']) ? strtoupper($_GET['order_direction']) : 'DESC';
    
    // Validate order parameters
    $allowed_order_fields = ['completion_time', 'start_time', 'end_time', 'route_distance', 'actual_duration', 'user_rating'];
    if (!in_array($order_by, $allowed_order_fields)) {
        $order_by = 'completion_time';
    }
    
    if (!in_array($order_direction, ['ASC', 'DESC'])) {
        $order_direction = 'DESC';
    }
    
    // Validate limit
    if ($limit > 100) {
        $limit = 100; // Max 100 records per request
    }
    
    // Check if database connection is available
    if (!class_exists('Database')) {
        // Return mock response if database is not available
        $mock_data = [
            [
                'id' => 1,
                'user_id' => $user_id,
                'start_latitude' => 14.62270180,
                'start_longitude' => 121.17656790,
                'end_latitude' => 14.59758003,
                'end_longitude' => 121.17244053,
                'destination_name' => 'Dela Paz Elementary School',
                'destination_address' => 'Antipolo City',
                'route_distance' => 3.27,
                'estimated_duration' => 6,
                'actual_duration' => 8,
                'estimated_fare' => 25.0,
                'actual_fare' => 30.0,
                'transport_mode' => 'driving',
                'success_rate' => 100.0,
                'completion_time' => '2025-09-13 20:59:30',
                'start_time' => '2025-09-13 20:59:14',
                'end_time' => '2025-09-13 21:07:14',
                'waypoints_count' => 5,
                'traffic_condition' => 'Moderate Traffic',
                'average_speed' => 24.5,
                'route_quality' => 'good',
                'user_rating' => 4,
                'notes' => 'Sample navigation history',
                'is_favorite' => 0,
                'created_at' => '2025-09-13 20:59:14',
                'updated_at' => '2025-09-13 21:07:14'
            ],
            [
                'id' => 2,
                'user_id' => $user_id,
                'start_latitude' => 14.62272000,
                'start_longitude' => 121.17658790,
                'end_latitude' => 14.62231419,
                'end_longitude' => 121.17618992,
                'destination_name' => 'Langhaya',
                'destination_address' => 'Antipolo City',
                'route_distance' => 0.06,
                'estimated_duration' => 1,
                'actual_duration' => 2,
                'estimated_fare' => 15.0,
                'actual_fare' => 15.0,
                'transport_mode' => 'driving',
                'success_rate' => 100.0,
                'completion_time' => '2025-09-13 21:00:01',
                'start_time' => '2025-09-13 21:00:01',
                'end_time' => '2025-09-13 21:02:01',
                'waypoints_count' => 1,
                'traffic_condition' => 'Moderate Traffic',
                'average_speed' => 1.8,
                'route_quality' => 'excellent',
                'user_rating' => 5,
                'notes' => 'Quick trip to Langhaya',
                'is_favorite' => 1,
                'created_at' => '2025-09-13 21:00:01',
                'updated_at' => '2025-09-13 21:02:01'
            ]
        ];
        
        echo json_encode([
            'success' => true,
            'message' => 'Navigation history retrieved successfully (mock)',
            'data' => [
                'history' => $mock_data,
                'pagination' => [
                    'total' => count($mock_data),
                    'limit' => $limit,
                    'offset' => $offset,
                    'has_more' => false
                ]
            ]
        ]);
        exit();
    }
    
    // Initialize database connection
    $db = new Database();
    $pdo = $db->getConnection();
    
    if (!$pdo) {
        throw new Exception('Database connection failed');
    }
    
    // Get total count
    $count_sql = "SELECT COUNT(*) as total FROM navigation_history WHERE user_id = :user_id";
    $count_stmt = $pdo->prepare($count_sql);
    $count_stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $count_stmt->execute();
    $total_count = $count_stmt->fetch(PDO::FETCH_ASSOC)['total'];
    
    // Prepare select statement
    $sql = "SELECT * FROM navigation_history 
            WHERE user_id = :user_id 
            ORDER BY $order_by $order_direction 
            LIMIT :limit OFFSET :offset";
    
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
    $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
    
    // Execute the statement
    $stmt->execute();
    $history_records = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Calculate pagination info
    $has_more = ($offset + $limit) < $total_count;
    
    // Return success response
    echo json_encode([
        'success' => true,
        'message' => 'Navigation history retrieved successfully',
        'data' => [
            'history' => $history_records,
            'pagination' => [
                'total' => (int)$total_count,
                'limit' => $limit,
                'offset' => $offset,
                'has_more' => $has_more
            ]
        ]
    ]);
    
} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Error retrieving navigation history: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>
