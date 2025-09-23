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
    
    // Get history ID from URL parameter
    $history_id = isset($_GET['id']) ? (int)$_GET['id'] : null;
    
    if (!$history_id || $history_id <= 0) {
        throw new Exception('Valid history ID is required');
    }
    
    // Get user_id from query parameter (optional for validation)
    $user_id = isset($_GET['user_id']) ? (int)$_GET['user_id'] : null;
    
    // Check if database connection is available
    if (!class_exists('Database')) {
        // Return mock response if database is not available
        echo json_encode([
            'success' => true,
            'message' => 'Navigation history retrieved successfully (mock)',
            'data' => [
                'id' => $history_id,
                'user_id' => $user_id ?? 10,
                'navigation_log_id' => rand(1, 100),
                'start_latitude' => 14.62270180,
                'start_longitude' => 121.17656790,
                'end_latitude' => 14.59758003,
                'end_longitude' => 121.17244053,
                'destination_name' => 'Sample Destination',
                'destination_address' => 'Sample Address, City',
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
    
    // Prepare select statement
    $sql = "SELECT * FROM navigation_history WHERE id = :id";
    $params = [':id' => $history_id];
    
    // Add user_id filter if provided
    if ($user_id) {
        $sql .= " AND user_id = :user_id";
        $params[':user_id'] = $user_id;
    }
    
    $stmt = $pdo->prepare($sql);
    
    // Bind parameters
    foreach ($params as $key => $value) {
        $stmt->bindValue($key, $value, PDO::PARAM_INT);
    }
    
    // Execute the statement
    $stmt->execute();
    $history_record = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$history_record) {
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'message' => 'Navigation history not found',
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    // Return success response
    echo json_encode([
        'success' => true,
        'message' => 'Navigation history retrieved successfully',
        'data' => $history_record
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

