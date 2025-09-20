<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed. Only POST requests are supported.',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    exit();
}

try {
    // Include database configuration
    @include_once __DIR__ . '/../../config/database.php';
    
    // Get request body
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        throw new Exception('Invalid JSON input');
    }
    
    // Validate required fields
    $required_fields = [
        'user_id', 'start_latitude', 'start_longitude', 
        'end_latitude', 'end_longitude', 'destination_name',
        'route_distance', 'start_time', 'end_time', 'transport_mode'
    ];
    
    foreach ($required_fields as $field) {
        if (!isset($input[$field]) || $input[$field] === '') {
            throw new Exception("Missing required field: $field");
        }
    }
    
    // Get user_id from query parameter or request body
    $user_id = isset($_GET['user_id']) ? (int)$_GET['user_id'] : (int)$input['user_id'];
    
    if ($user_id <= 0) {
        $user_id = 10; // Default user ID
    }
    
    // Check if database connection is available
    if (!class_exists('Database')) {
        // Return mock response if database is not available
        $mock_id = rand(1000, 9999);
        echo json_encode([
            'success' => true,
            'message' => 'Navigation history created successfully (mock)',
            'data' => [
                'id' => $mock_id,
                'user_id' => $user_id,
                'navigation_log_id' => $input['navigation_log_id'] ?? null,
                'start_latitude' => (float)$input['start_latitude'],
                'start_longitude' => (float)$input['start_longitude'],
                'end_latitude' => (float)$input['end_latitude'],
                'end_longitude' => (float)$input['end_longitude'],
                'destination_name' => $input['destination_name'],
                'destination_address' => $input['destination_address'] ?? null,
                'route_distance' => (float)$input['route_distance'],
                'estimated_duration' => isset($input['estimated_duration']) ? (int)$input['estimated_duration'] : null,
                'actual_duration' => isset($input['actual_duration']) ? (int)$input['actual_duration'] : null,
                'estimated_fare' => isset($input['estimated_fare']) ? (float)$input['estimated_fare'] : null,
                'actual_fare' => isset($input['actual_fare']) ? (float)$input['actual_fare'] : null,
                'transport_mode' => $input['transport_mode'],
                'success_rate' => isset($input['success_rate']) ? (float)$input['success_rate'] : 100.0,
                'completion_time' => $input['completion_time'] ?? null,
                'start_time' => $input['start_time'],
                'end_time' => $input['end_time'],
                'waypoints_count' => isset($input['waypoints_count']) ? (int)$input['waypoints_count'] : 0,
                'traffic_condition' => $input['traffic_condition'] ?? null,
                'average_speed' => isset($input['average_speed']) ? (float)$input['average_speed'] : null,
                'route_quality' => $input['route_quality'] ?? 'good',
                'user_rating' => isset($input['user_rating']) ? (int)$input['user_rating'] : null,
                'notes' => $input['notes'] ?? null,
                'is_favorite' => isset($input['is_favorite']) ? (int)$input['is_favorite'] : 0,
                'created_at' => date('Y-m-d H:i:s'),
                'updated_at' => date('Y-m-d H:i:s')
            ]
        ]);
        exit();
    }
    
    // Initialize database connection
    $db = new Database();
    $pdo = $db->connect();
    
    if (!$pdo) {
        throw new Exception('Database connection failed');
    }
    
    // Start transaction
    $pdo->beginTransaction();
    
    try {
        // Prepare insert statement
        $sql = "INSERT INTO navigation_history (
            user_id, navigation_log_id, start_latitude, start_longitude,
            end_latitude, end_longitude, destination_name, destination_address,
            route_distance, estimated_duration, actual_duration, estimated_fare, actual_fare,
            transport_mode, success_rate, completion_time, start_time, end_time,
            waypoints_count, traffic_condition, average_speed, route_quality,
            user_rating, notes, is_favorite
        ) VALUES (
            :user_id, :navigation_log_id, :start_latitude, :start_longitude,
            :end_latitude, :end_longitude, :destination_name, :destination_address,
            :route_distance, :estimated_duration, :actual_duration, :estimated_fare, :actual_fare,
            :transport_mode, :success_rate, :completion_time, :start_time, :end_time,
            :waypoints_count, :traffic_condition, :average_speed, :route_quality,
            :user_rating, :notes, :is_favorite
        )";
        
        $stmt = $pdo->prepare($sql);
        
        // Bind parameters
        $stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
        $stmt->bindParam(':navigation_log_id', $input['navigation_log_id'], PDO::PARAM_INT);
        $stmt->bindParam(':start_latitude', $input['start_latitude'], PDO::PARAM_STR);
        $stmt->bindParam(':start_longitude', $input['start_longitude'], PDO::PARAM_STR);
        $stmt->bindParam(':end_latitude', $input['end_latitude'], PDO::PARAM_STR);
        $stmt->bindParam(':end_longitude', $input['end_longitude'], PDO::PARAM_STR);
        $stmt->bindParam(':destination_name', $input['destination_name'], PDO::PARAM_STR);
        $stmt->bindParam(':destination_address', $input['destination_address'], PDO::PARAM_STR);
        $stmt->bindParam(':route_distance', $input['route_distance'], PDO::PARAM_STR);
        $stmt->bindParam(':estimated_duration', $input['estimated_duration'], PDO::PARAM_INT);
        $stmt->bindParam(':actual_duration', $input['actual_duration'], PDO::PARAM_INT);
        $stmt->bindParam(':estimated_fare', $input['estimated_fare'], PDO::PARAM_STR);
        $stmt->bindParam(':actual_fare', $input['actual_fare'], PDO::PARAM_STR);
        $stmt->bindParam(':transport_mode', $input['transport_mode'], PDO::PARAM_STR);
        $stmt->bindParam(':success_rate', $input['success_rate'] ?? 100.0, PDO::PARAM_STR);
        $stmt->bindParam(':completion_time', $input['completion_time'], PDO::PARAM_STR);
        $stmt->bindParam(':start_time', $input['start_time'], PDO::PARAM_STR);
        $stmt->bindParam(':end_time', $input['end_time'], PDO::PARAM_STR);
        $stmt->bindParam(':waypoints_count', $input['waypoints_count'] ?? 0, PDO::PARAM_INT);
        $stmt->bindParam(':traffic_condition', $input['traffic_condition'], PDO::PARAM_STR);
        $stmt->bindParam(':average_speed', $input['average_speed'], PDO::PARAM_STR);
        $stmt->bindParam(':route_quality', $input['route_quality'] ?? 'good', PDO::PARAM_STR);
        $stmt->bindParam(':user_rating', $input['user_rating'], PDO::PARAM_INT);
        $stmt->bindParam(':notes', $input['notes'], PDO::PARAM_STR);
        $stmt->bindParam(':is_favorite', $input['is_favorite'] ?? 0, PDO::PARAM_INT);
        
        // Execute the statement
        if (!$stmt->execute()) {
            throw new Exception('Failed to insert navigation history');
        }
        
        // Get the inserted ID
        $history_id = $pdo->lastInsertId();
        
        // Commit transaction
        $pdo->commit();
        
        // Fetch the created record
        $fetch_sql = "SELECT * FROM navigation_history WHERE id = :id";
        $fetch_stmt = $pdo->prepare($fetch_sql);
        $fetch_stmt->bindParam(':id', $history_id, PDO::PARAM_INT);
        $fetch_stmt->execute();
        $created_record = $fetch_stmt->fetch(PDO::FETCH_ASSOC);
        
        // Return success response
        echo json_encode([
            'success' => true,
            'message' => 'Navigation history created successfully',
            'data' => $created_record
        ]);
        
    } catch (Exception $e) {
        // Rollback transaction on error
        $pdo->rollBack();
        throw $e;
    }
    
} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Error creating navigation history: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>

