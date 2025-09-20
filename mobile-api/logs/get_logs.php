<?php
/**
 * Get Logs Endpoint
 * GET /logs/get
 * 
 * Retrieves logs based on log_type parameter
 * Supports both navigation logs and user activity logs
 */

require_once '../config/database.php';
require_once '../includes/Response.php';
require_once '../includes/Validator.php';
require_once '../includes/SessionManager.php';

try {
    // Require authentication
    SessionManager::requireAuth();
    
    $currentUser = SessionManager::getUserData();
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get query parameters
    $logType = isset($_GET['log_type']) ? Validator::sanitizeString($_GET['log_type']) : 'all';
    $page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
    $limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 20;
    
    $offset = ($page - 1) * $limit;
    
    // Route to appropriate log retrieval based on type
    switch ($logType) {
        case 'navigation':
            getNavigationLogs($conn, $currentUser, $page, $limit, $offset);
            break;
        case 'user_activity':
            getUserActivityLogs($conn, $currentUser, $page, $limit, $offset);
            break;
        case 'all':
            getAllLogs($conn, $currentUser, $page, $limit, $offset);
            break;
        default:
            Response::error('Invalid log_type. Must be "navigation", "user_activity", or "all"');
    }
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve logs: ' . $e->getMessage());
}

/**
 * Get Navigation Logs
 */
function getNavigationLogs($conn, $currentUser, $page, $limit, $offset) {
    // Get additional query parameters for navigation logs
    $activityType = isset($_GET['activity_type']) ? Validator::sanitizeString($_GET['activity_type']) : '';
    $transportMode = isset($_GET['transport_mode']) ? Validator::sanitizeString($_GET['transport_mode']) : '';
    $dateFrom = isset($_GET['date_from']) ? Validator::sanitizeString($_GET['date_from']) : '';
    $dateTo = isset($_GET['date_to']) ? Validator::sanitizeString($_GET['date_to']) : '';
    
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
        'log_type' => 'navigation',
        'pagination' => [
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $total,
            'items_per_page' => $limit,
            'has_next' => $page < $totalPages,
            'has_prev' => $page > 1
        ]
    ], 'Navigation logs retrieved successfully');
}

/**
 * Get User Activity Logs
 */
function getUserActivityLogs($conn, $currentUser, $page, $limit, $offset) {
    // Get additional query parameters for user activity logs
    $logTypeActivity = isset($_GET['log_type_activity']) ? Validator::sanitizeString($_GET['log_type_activity']) : '';
    $logLevel = isset($_GET['log_level']) ? Validator::sanitizeString($_GET['log_level']) : '';
    $dateFrom = isset($_GET['date_from']) ? Validator::sanitizeString($_GET['date_from']) : '';
    $dateTo = isset($_GET['date_to']) ? Validator::sanitizeString($_GET['date_to']) : '';
    
    // Build query - users can only see their own logs
    $whereConditions = ['user_name = ?'];
    $params = [$currentUser['first_name'] . ' ' . $currentUser['last_name']];
    
    if (!empty($logTypeActivity)) {
        $whereConditions[] = "log_type = ?";
        $params[] = $logTypeActivity;
    }
    
    if (!empty($logLevel)) {
        $whereConditions[] = "log_level = ?";
        $params[] = $logLevel;
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
    $countStmt = $conn->prepare("SELECT COUNT(*) as total FROM user_activity_logs WHERE $whereClause");
    $countStmt->execute($params);
    $total = $countStmt->fetch()['total'];
    
    // Get logs
    $logsStmt = $conn->prepare("
        SELECT id, log_type, log_level, user_name, action, message, user_agent, additional_data, created_at
        FROM user_activity_logs 
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
        if ($log['additional_data']) {
            $log['additional_data'] = json_decode($log['additional_data'], true);
        }
    }
    
    // Calculate pagination info
    $totalPages = ceil($total / $limit);
    
    Response::success([
        'logs' => $logs,
        'log_type' => 'user_activity',
        'pagination' => [
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $total,
            'items_per_page' => $limit,
            'has_next' => $page < $totalPages,
            'has_prev' => $page > 1
        ]
    ], 'User activity logs retrieved successfully');
}

/**
 * Get All Logs (Combined)
 */
function getAllLogs($conn, $currentUser, $page, $limit, $offset) {
    // Get additional query parameters
    $dateFrom = isset($_GET['date_from']) ? Validator::sanitizeString($_GET['date_from']) : '';
    $dateTo = isset($_GET['date_to']) ? Validator::sanitizeString($_GET['date_to']) : '';
    
    $allLogs = [];
    
    // Get navigation logs
    $navWhereConditions = ['user_id = ?'];
    $navParams = [$currentUser['id']];
    
    if (!empty($dateFrom)) {
        $navWhereConditions[] = "DATE(created_at) >= ?";
        $navParams[] = $dateFrom;
    }
    
    if (!empty($dateTo)) {
        $navWhereConditions[] = "DATE(created_at) <= ?";
        $navParams[] = $dateTo;
    }
    
    $navWhereClause = implode(' AND ', $navWhereConditions);
    
    $navStmt = $conn->prepare("
        SELECT id, user_id, user_name, activity_type, start_latitude, start_longitude, 
               end_latitude, end_longitude, destination_name, destination_address, 
               route_distance, estimated_duration, transport_mode, navigation_duration,
               route_instructions, waypoints, destination_reached, device_info, 
               app_version, os_version, additional_data, error_message, created_at, updated_at,
               'navigation' as log_type
        FROM navigation_activity_logs 
        WHERE $navWhereClause
        ORDER BY created_at DESC
    ");
    $navStmt->execute($navParams);
    $navLogs = $navStmt->fetchAll();
    
    // Parse JSON fields for navigation logs
    foreach ($navLogs as &$log) {
        if ($log['waypoints']) {
            $log['waypoints'] = json_decode($log['waypoints'], true);
        }
        if ($log['additional_data']) {
            $log['additional_data'] = json_decode($log['additional_data'], true);
        }
    }
    
    // Get user activity logs
    $userWhereConditions = ['user_name = ?'];
    $userParams = [$currentUser['first_name'] . ' ' . $currentUser['last_name']];
    
    if (!empty($dateFrom)) {
        $userWhereConditions[] = "DATE(created_at) >= ?";
        $userParams[] = $dateFrom;
    }
    
    if (!empty($dateTo)) {
        $userWhereConditions[] = "DATE(created_at) <= ?";
        $userParams[] = $dateTo;
    }
    
    $userWhereClause = implode(' AND ', $userWhereConditions);
    
    $userStmt = $conn->prepare("
        SELECT id, log_type, log_level, user_name, action, message, user_agent, additional_data, created_at,
               'user_activity' as log_type_category
        FROM user_activity_logs 
        WHERE $userWhereClause
        ORDER BY created_at DESC
    ");
    $userStmt->execute($userParams);
    $userLogs = $userStmt->fetchAll();
    
    // Parse JSON fields for user activity logs
    foreach ($userLogs as &$log) {
        if ($log['additional_data']) {
            $log['additional_data'] = json_decode($log['additional_data'], true);
        }
    }
    
    // Combine and sort all logs by created_at
    $allLogs = array_merge($navLogs, $userLogs);
    usort($allLogs, function($a, $b) {
        return strtotime($b['created_at']) - strtotime($a['created_at']);
    });
    
    // Apply pagination
    $total = count($allLogs);
    $paginatedLogs = array_slice($allLogs, $offset, $limit);
    
    // Calculate pagination info
    $totalPages = ceil($total / $limit);
    
    Response::success([
        'logs' => $paginatedLogs,
        'log_type' => 'all',
        'pagination' => [
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $total,
            'items_per_page' => $limit,
            'has_next' => $page < $totalPages,
            'has_prev' => $page > 1
        ]
    ], 'All logs retrieved successfully');
}
?>

