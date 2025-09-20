<?php
/**
 * Get Navigation Logs Endpoint
 * GET /navigation-logs
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/SessionManager.php';

try {
    // Require authentication
    SessionManager::requireAuth();
    
    $currentUser = SessionManager::getUserData();
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get query parameters
    $page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
    $limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 20;
    $activityType = isset($_GET['activity_type']) ? Validator::sanitizeString($_GET['activity_type']) : '';
    $transportMode = isset($_GET['transport_mode']) ? Validator::sanitizeString($_GET['transport_mode']) : '';
    $dateFrom = isset($_GET['date_from']) ? Validator::sanitizeString($_GET['date_from']) : '';
    $dateTo = isset($_GET['date_to']) ? Validator::sanitizeString($_GET['date_to']) : '';
    
    $offset = ($page - 1) * $limit;
    
    // Build query - users can only see their own logs
    $whereConditions = ['user_id = ?'];
    $params = [$currentUser['id']];
    
    if (!empty($activityType)) {
        $whereConditions[] = "activity_type = ?";
        $params[] = $activityType;
    }
    
    if (!empty($transportMode)) {
        $whereConditions[] = "transport_mode = ?";
        $params[] = $transportMode;
    }
    
    if (!empty($dateFrom)) {
        $whereConditions[] = "DATE(created_at) >= ?";
        $params[] = $dateFrom;
    }
    
    if (!empty($dateTo)) {
        $whereConditions[] = "DATE(created_at) <= ?";
        $params[] = $dateTo;
    }
    
    $whereClause = implode(' AND ', $whereConditions);
    
    // Get total count
    $countStmt = $conn->prepare("SELECT COUNT(*) as total FROM navigation_activity_logs WHERE $whereClause");
    $countStmt->execute($params);
    $total = $countStmt->fetch()['total'];
    
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
        LIMIT ? OFFSET ?
    ");
    
    $params[] = $limit;
    $params[] = $offset;
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
    }
    
    // Calculate pagination info
    $totalPages = ceil($total / $limit);
    
    Response::success([
        'logs' => $logs,
        'pagination' => [
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $total,
            'items_per_page' => $limit,
            'has_next' => $page < $totalPages,
            'has_prev' => $page > 1
        ]
    ], 'Navigation logs retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve navigation logs: ' . $e->getMessage());
}
?>

