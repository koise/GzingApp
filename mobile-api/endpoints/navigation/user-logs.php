<?php
/**
 * Get User Navigation Logs Endpoint
 * GET /navigation_activity_logs/user-logs
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/SessionManager.php';

try {
    // Enable error reporting for debugging
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
    
    // For now, allow access without authentication for testing
    // TODO: Implement proper authentication later
    // SessionManager::requireAuth();
    // $currentUser = SessionManager::getUserData();
    
    // Debug: Log the request
    error_log("User logs endpoint called with user_id: " . ($_GET['user_id'] ?? 'not set'));
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get query parameters
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 10; // Default user ID for testing
    $limit = isset($_GET['limit']) ? min(1000, max(1, intval($_GET['limit']))) : 100;
    $activityType = isset($_GET['activity_type']) ? $_GET['activity_type'] : null;
    $transportMode = isset($_GET['transport_mode']) ? $_GET['transport_mode'] : null;
    $dateFrom = isset($_GET['date_from']) ? $_GET['date_from'] : null;
    $dateTo = isset($_GET['date_to']) ? $_GET['date_to'] : null;
    
    // Build query conditions
    $whereConditions = ['user_id = ?'];
    $params = [$userId];
    
    if ($activityType) {
        $whereConditions[] = 'activity_type = ?';
        $params[] = $activityType;
    }
    
    if ($transportMode) {
        $whereConditions[] = 'transport_mode = ?';
        $params[] = $transportMode;
    }
    
    if ($dateFrom) {
        $whereConditions[] = 'DATE(created_at) >= ?';
        $params[] = $dateFrom;
    }
    
    if ($dateTo) {
        $whereConditions[] = 'DATE(created_at) <= ?';
        $params[] = $dateTo;
    }
    
    $whereClause = implode(' AND ', $whereConditions);
    
    // Get logs
    $logsStmt = $conn->prepare("
        SELECT id, user_id, user_name, activity_type, start_latitude, start_longitude, 
               end_latitude, end_longitude, destination_name, destination_address, 
               route_distance, estimated_duration, transport_mode, navigation_duration,
               route_instructions, waypoints, destination_reached, device_info, 
               app_version, os_version, additional_data, error_message, created_at, updated_at
        FROM navigation_activity_logs 
        WHERE $whereClause
        ORDER BY created_at DESC
        LIMIT ?
    ");
    
    $params[] = $limit;
    $logsStmt->execute($params);
    $logs = $logsStmt->fetchAll();
    
    // Parse JSON fields
    foreach ($logs as &$log) {
        if ($log['waypoints']) {
            $log['waypoints'] = json_decode($log['waypoints'], true);
        }
        if ($log['additional_data']) {
            $log['additional_data'] = json_decode($log['additional_data'], true);
        }
        
        // Convert boolean fields
        $log['destination_reached'] = (bool) $log['destination_reached'];
        
        // Convert numeric fields
        $log['route_distance'] = $log['route_distance'] ? floatval($log['route_distance']) : null;
        $log['estimated_duration'] = $log['estimated_duration'] ? intval($log['estimated_duration']) : null;
        $log['navigation_duration'] = $log['navigation_duration'] ? intval($log['navigation_duration']) : null;
        $log['start_latitude'] = $log['start_latitude'] ? floatval($log['start_latitude']) : null;
        $log['start_longitude'] = $log['start_longitude'] ? floatval($log['start_longitude']) : null;
        $log['end_latitude'] = $log['end_latitude'] ? floatval($log['end_latitude']) : null;
        $log['end_longitude'] = $log['end_longitude'] ? floatval($log['end_longitude']) : null;
    }
    
    // Get total count for pagination info
    $countStmt = $conn->prepare("SELECT COUNT(*) as total FROM navigation_activity_logs WHERE $whereClause");
    $countStmt->execute(array_slice($params, 0, -1)); // Remove the limit parameter
    $total = $countStmt->fetch()['total'];
    
    Response::success([
        'logs' => $logs,
        'pagination' => [
            'total_items' => intval($total),
            'returned_items' => count($logs),
            'limit' => $limit,
            'has_more' => count($logs) >= $limit
        ],
        'filters' => [
            'user_id' => $userId,
            'activity_type' => $activityType,
            'transport_mode' => $transportMode,
            'date_from' => $dateFrom,
            'date_to' => $dateTo
        ]
    ], 'User navigation logs retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve user navigation logs: ' . $e->getMessage());
}
?>
