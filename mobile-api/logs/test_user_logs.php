<?php
/**
 * Test version of user-logs endpoint
 */

header('Content-Type: application/json');

try {
    // Test basic functionality first
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 10;
    $limit = isset($_GET['limit']) ? min(1000, max(1, intval($_GET['limit']))) : 100;
    
    // Test database connection
    require_once '../config/database.php';
    $db = new Database();
    $conn = $db->getConnection();
    
    // Test simple query
    $stmt = $conn->prepare("SELECT COUNT(*) as count FROM navigation_activity_logs WHERE user_id = ?");
    $stmt->execute([$userId]);
    $result = $stmt->fetch();
    
    // Get actual logs
    $logsStmt = $conn->prepare("
        SELECT id, user_id, user_name, activity_type, start_latitude, start_longitude, 
               end_latitude, end_longitude, destination_name, destination_address, 
               route_distance, estimated_duration, transport_mode, navigation_duration,
               route_instructions, waypoints, destination_reached, device_info, 
               app_version, os_version, additional_data, error_message, created_at, updated_at
        FROM navigation_activity_logs 
        WHERE user_id = ?
        ORDER BY created_at DESC
        LIMIT ?
    ");
    
    $logsStmt->execute([$userId, $limit]);
    $logs = $logsStmt->fetchAll();
    
    echo json_encode([
        'success' => true,
        'message' => 'User logs retrieved successfully',
        'data' => [
            'logs' => $logs,
            'total_count' => $result['count'],
            'returned_count' => count($logs),
            'user_id' => $userId,
            'limit' => $limit
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage(),
        'file' => $e->getFile(),
        'line' => $e->getLine(),
        'trace' => $e->getTraceAsString()
    ]);
}
?>

