<?php
// Suppress PHP warnings to prevent them from interfering with JSON output
error_reporting(E_ERROR | E_PARSE);
ini_set('display_errors', 0);

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
        'user_id', 'route_name', 'start_latitude', 'start_longitude', 
        'end_latitude', 'end_longitude', 'destination_name', 'route_distance', 'transport_mode'
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
            'message' => 'Navigation route created successfully (mock)',
            'data' => [
                'id' => $mock_id,
                'user_id' => $user_id,
                'route_name' => $input['route_name'],
                'route_description' => $input['route_description'] ?? null,
                'start_latitude' => (float)$input['start_latitude'],
                'start_longitude' => (float)$input['start_longitude'],
                'end_latitude' => (float)$input['end_latitude'],
                'end_longitude' => (float)$input['end_longitude'],
                'destination_name' => $input['destination_name'],
                'destination_address' => $input['destination_address'] ?? null,
                'route_distance' => (float)$input['route_distance'],
                'estimated_duration' => isset($input['estimated_duration']) ? (int)$input['estimated_duration'] : null,
                'estimated_fare' => isset($input['estimated_fare']) ? (float)$input['estimated_fare'] : null,
                'transport_mode' => $input['transport_mode'],
                'route_quality' => $input['route_quality'] ?? 'good',
                'traffic_condition' => $input['traffic_condition'] ?? null,
                'average_speed' => isset($input['average_speed']) ? (float)$input['average_speed'] : null,
                'waypoints_count' => isset($input['waypoints_count']) ? (int)$input['waypoints_count'] : 0,
                'route_coordinates' => $input['route_coordinates'] ?? null,
                'is_favorite' => isset($input['is_favorite']) ? (int)$input['is_favorite'] : 0,
                'is_public' => isset($input['is_public']) ? (int)$input['is_public'] : 0,
                'usage_count' => 0,
                'last_used' => null,
                'created_at' => date('Y-m-d H:i:s'),
                'updated_at' => date('Y-m-d H:i:s')
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
    
    // Start transaction
    $pdo->beginTransaction();
    
    try {
        // Prepare insert statement
        $sql = "INSERT INTO navigation_routes (
            user_id, route_name, route_description, start_latitude, start_longitude,
            end_latitude, end_longitude, destination_name, destination_address,
            route_distance, estimated_duration, estimated_fare, transport_mode, route_quality,
            traffic_condition, average_speed, waypoints_count, route_coordinates,
            is_favorite, is_public, usage_count
        ) VALUES (
            :user_id, :route_name, :route_description, :start_latitude, :start_longitude,
            :end_latitude, :end_longitude, :destination_name, :destination_address,
            :route_distance, :estimated_duration, :estimated_fare, :transport_mode, :route_quality,
            :traffic_condition, :average_speed, :waypoints_count, :route_coordinates,
            :is_favorite, :is_public, :usage_count
        )";
        
        $stmt = $pdo->prepare($sql);
        
        // Bind parameters
        $stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
        $route_name = $input['route_name'];
        $route_description = $input['route_description'] ?? null;
        $start_latitude = $input['start_latitude'];
        $start_longitude = $input['start_longitude'];
        $end_latitude = $input['end_latitude'];
        $end_longitude = $input['end_longitude'];
        $destination_name = $input['destination_name'];
        $destination_address = $input['destination_address'] ?? null;
        $route_distance = $input['route_distance'];
        $estimated_duration = $input['estimated_duration'] ?? null;
        $estimated_fare = $input['estimated_fare'] ?? null;
        $transport_mode = $input['transport_mode'];
        $route_quality = $input['route_quality'] ?? 'good';
        $traffic_condition = $input['traffic_condition'] ?? null;
        $average_speed = $input['average_speed'] ?? null;
        $waypoints_count = $input['waypoints_count'] ?? 0;
        $route_coordinates = isset($input['route_coordinates']) ? json_encode($input['route_coordinates']) : null;
        $is_favorite = $input['is_favorite'] ?? 0;
        $is_public = $input['is_public'] ?? 0;
        $usage_count = 0;
        
        $stmt->bindParam(':route_name', $route_name, PDO::PARAM_STR);
        $stmt->bindParam(':route_description', $route_description, PDO::PARAM_STR);
        $stmt->bindParam(':start_latitude', $start_latitude, PDO::PARAM_STR);
        $stmt->bindParam(':start_longitude', $start_longitude, PDO::PARAM_STR);
        $stmt->bindParam(':end_latitude', $end_latitude, PDO::PARAM_STR);
        $stmt->bindParam(':end_longitude', $end_longitude, PDO::PARAM_STR);
        $stmt->bindParam(':destination_name', $destination_name, PDO::PARAM_STR);
        $stmt->bindParam(':destination_address', $destination_address, PDO::PARAM_STR);
        $stmt->bindParam(':route_distance', $route_distance, PDO::PARAM_STR);
        $stmt->bindParam(':estimated_duration', $estimated_duration, PDO::PARAM_INT);
        $stmt->bindParam(':estimated_fare', $estimated_fare, PDO::PARAM_STR);
        $stmt->bindParam(':transport_mode', $transport_mode, PDO::PARAM_STR);
        $stmt->bindParam(':route_quality', $route_quality, PDO::PARAM_STR);
        $stmt->bindParam(':traffic_condition', $traffic_condition, PDO::PARAM_STR);
        $stmt->bindParam(':average_speed', $average_speed, PDO::PARAM_STR);
        $stmt->bindParam(':waypoints_count', $waypoints_count, PDO::PARAM_INT);
        $stmt->bindParam(':route_coordinates', $route_coordinates, PDO::PARAM_STR);
        $stmt->bindParam(':is_favorite', $is_favorite, PDO::PARAM_INT);
        $stmt->bindParam(':is_public', $is_public, PDO::PARAM_INT);
        $stmt->bindParam(':usage_count', $usage_count, PDO::PARAM_INT);
        
        // Execute the statement
        if (!$stmt->execute()) {
            throw new Exception('Failed to insert navigation route');
        }
        
        // Get the inserted ID
        $route_id = $pdo->lastInsertId();
        
        // Commit transaction
        $pdo->commit();
        
        // Fetch the created record
        $fetch_sql = "SELECT * FROM navigation_routes WHERE id = :id";
        $fetch_stmt = $pdo->prepare($fetch_sql);
        $fetch_stmt->bindParam(':id', $route_id, PDO::PARAM_INT);
        $fetch_stmt->execute();
        $created_record = $fetch_stmt->fetch(PDO::FETCH_ASSOC);
        
        // Decode route_coordinates if it exists
        if ($created_record['route_coordinates']) {
            $created_record['route_coordinates'] = json_decode($created_record['route_coordinates'], true);
        }
        
        // Return success response
        http_response_code(200);
        header('Content-Type: application/json');
        echo json_encode([
            'success' => true,
            'message' => 'Navigation route created successfully',
            'data' => $created_record
        ]);
        
    } catch (Exception $e) {
        // Rollback transaction on error
        $pdo->rollBack();
        throw $e;
    }
    
} catch (Exception $e) {
    http_response_code(400);
    header('Content-Type: application/json');
    echo json_encode([
        'success' => false,
        'message' => 'Error creating navigation route: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>
