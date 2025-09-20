<?php
/**
 * Fallback Create Navigation Log Endpoint
 * Returns success response without database connection
 */

// Set content type and CORS headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

try {
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Invalid JSON input',
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    // Validate required fields
    $requiredFields = ['activity_type', 'start_latitude', 'start_longitude', 'destination_name'];
    $missingFields = [];
    
    foreach ($requiredFields as $field) {
        if (!isset($input[$field]) || empty($input[$field])) {
            $missingFields[] = $field;
        }
    }
    
    if (!empty($missingFields)) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Missing required fields: ' . implode(', ', $missingFields),
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    // Generate a mock log ID
    $mockLogId = rand(1000, 9999);
    
    // Return success response with mock data
    http_response_code(201);
    echo json_encode([
        'success' => true,
        'message' => 'Navigation log created successfully (fallback mode)',
        'data' => [
            'log_id' => $mockLogId,
            'activity_type' => $input['activity_type'],
            'start_latitude' => $input['start_latitude'],
            'start_longitude' => $input['start_longitude'],
            'destination_name' => $input['destination_name'],
            'destination_address' => $input['destination_address'] ?? null,
            'route_distance' => $input['route_distance'] ?? 0,
            'estimated_duration' => $input['estimated_duration'] ?? 0,
            'transport_mode' => $input['transport_mode'] ?? 'driving',
            'status' => 'created',
            'created_at' => date('Y-m-d H:i:s'),
            'updated_at' => date('Y-m-d H:i:s')
        ],
        'timestamp' => date('Y-m-d H:i:s'),
        'note' => 'This is fallback data. Database connection is not available. Log was not actually saved.'
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Failed to create navigation log: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>

