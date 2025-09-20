<?php
/**
 * Debug version of user-logs endpoint
 */

// Set error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set content type
header('Content-Type: application/json');

try {
    // Test basic functionality
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 10;
    $limit = isset($_GET['limit']) ? min(1000, max(1, intval($_GET['limit']))) : 100;
    
    // Test database connection
    require_once '../../config/database.php';
    $db = new Database();
    $conn = $db->getConnection();
    
    // Test simple query first
    $countStmt = $conn->prepare("SELECT COUNT(*) as count FROM navigation_activity_logs WHERE user_id = ?");
    $countStmt->execute([$userId]);
    $countResult = $countStmt->fetch();
    
    // Get logs
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
    
    // Return success response
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'User navigation logs retrieved successfully',
        'data' => [
            'logs' => $logs,
            'pagination' => [
                'total_count' => intval($countResult['count']),
                'returned_items' => count($logs),
                'limit' => $limit,
                'has_more' => count($logs) >= $limit
            ]
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
} catch (Exception $e) {
    // Return error response
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Failed to retrieve user navigation logs: ' . $e->getMessage(),
        'error_details' => [
            'file' => $e->getFile(),
            'line' => $e->getLine(),
            'trace' => $e->getTraceAsString()
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ]);
} catch (Error $e) {
    // Catch fatal errors
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Fatal error: ' . $e->getMessage(),
        'error_details' => [
            'file' => $e->getFile(),
            'line' => $e->getLine(),
            'trace' => $e->getTraceAsString()
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>

