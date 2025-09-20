<?php
/**
 * Create Log Endpoint
 * POST /logs/create
 * 
 * Creates either navigation logs or user activity logs based on log_type
 */

require_once '../config/database.php';
require_once '../includes/Response.php';
require_once '../includes/Validator.php';
require_once '../includes/SessionManager.php';

try {
    // Require authentication
    SessionManager::requireAuth();
    
    $currentUser = SessionManager::getUserData();
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input');
    }
    
    // Validate required fields
    $requiredFields = ['log_type'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    $logType = Validator::sanitizeString($input['log_type']);
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Route to appropriate log creation based on type
    switch ($logType) {
        case 'navigation':
            createNavigationLog($conn, $currentUser, $input);
            break;
        case 'user_activity':
            createUserActivityLog($conn, $currentUser, $input);
            break;
        default:
            Response::error('Invalid log_type. Must be "navigation" or "user_activity"');
    }
    
} catch (Exception $e) {
    Response::serverError('Failed to create log: ' . $e->getMessage());
}

/**
 * Create Navigation Log
 */
function createNavigationLog($conn, $currentUser, $input) {
    // Validate required fields for navigation logs
    $requiredFields = ['activity_type'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $activityType = Validator::sanitizeString($input['activity_type']);
    $startLatitude = isset($input['start_latitude']) ? floatval($input['start_latitude']) : null;
    $startLongitude = isset($input['start_longitude']) ? floatval($input['start_longitude']) : null;
    $endLatitude = isset($input['end_latitude']) ? floatval($input['end_latitude']) : null;
    $endLongitude = isset($input['end_longitude']) ? floatval($input['end_longitude']) : null;
    $destinationName = isset($input['destination_name']) ? Validator::sanitizeString($input['destination_name']) : null;
    $destinationAddress = isset($input['destination_address']) ? Validator::sanitizeString($input['destination_address']) : null;
    $routeDistance = isset($input['route_distance']) ? max(0, floatval($input['route_distance'])) : null;
    $estimatedDuration = isset($input['estimated_duration']) ? max(0, intval($input['estimated_duration'])) : null;
    $transportMode = isset($input['transport_mode']) ? Validator::sanitizeString($input['transport_mode']) : null;
    $navigationDuration = isset($input['navigation_duration']) ? max(0, intval($input['navigation_duration'])) : null;
    $routeInstructions = isset($input['route_instructions']) ? Validator::sanitizeString($input['route_instructions']) : null;
    $waypoints = isset($input['waypoints']) ? $input['waypoints'] : null;
    $destinationReached = isset($input['destination_reached']) ? filter_var($input['destination_reached'], FILTER_VALIDATE_BOOLEAN) : false;
    $deviceInfo = isset($input['device_info']) ? Validator::sanitizeString($input['device_info']) : null;
    $appVersion = isset($input['app_version']) ? Validator::sanitizeString($input['app_version']) : null;
    $osVersion = isset($input['os_version']) ? Validator::sanitizeString($input['os_version']) : null;
    $additionalData = isset($input['additional_data']) ? $input['additional_data'] : null;
    $errorMessage = isset($input['error_message']) ? Validator::sanitizeString($input['error_message']) : null;
    
    // Validate activity type
    $validActivityTypes = ['navigation_start', 'navigation_stop', 'navigation_pause', 'navigation_resume', 'route_change', 'destination_reached'];
    if (!in_array($activityType, $validActivityTypes)) {
        Response::error('Invalid activity type. Must be one of: ' . implode(', ', $validActivityTypes));
    }
    
    // Validate transport mode if provided
    if ($transportMode) {
        $validTransportModes = ['driving', 'walking', 'cycling', 'transit'];
        if (!in_array($transportMode, $validTransportModes)) {
            Response::error('Invalid transport mode. Must be one of: ' . implode(', ', $validTransportModes));
        }
    }
    
    // Validate coordinates if provided
    if ($startLatitude !== null && $startLongitude !== null) {
        if (!Validator::validateCoordinates($startLatitude, $startLongitude)) {
            Response::error('Invalid start coordinates');
        }
    }
    
    if ($endLatitude !== null && $endLongitude !== null) {
        if (!Validator::validateCoordinates($endLatitude, $endLongitude)) {
            Response::error('Invalid end coordinates');
        }
    }
    
    // Validate JSON fields
    if ($waypoints && !Validator::validateJson(json_encode($waypoints))) {
        Response::error('Invalid waypoints format');
    }
    
    if ($additionalData && !Validator::validateJson(json_encode($additionalData))) {
        Response::error('Invalid additional_data format');
    }
    
    // Insert new navigation log
    $insertStmt = $conn->prepare("
        INSERT INTO navigation_activity_logs (
            user_id, user_name, activity_type, start_latitude, start_longitude, 
            end_latitude, end_longitude, destination_name, destination_address, 
            route_distance, estimated_duration, transport_mode, navigation_duration,
            route_instructions, waypoints, destination_reached, device_info, 
            app_version, os_version, additional_data, error_message, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
    ");
    
    $waypointsJson = $waypoints ? json_encode($waypoints) : null;
    $additionalDataJson = $additionalData ? json_encode($additionalData) : null;
    
    $insertStmt->execute([
        $currentUser['id'],
        $currentUser['first_name'] . ' ' . $currentUser['last_name'],
        $activityType,
        $startLatitude,
        $startLongitude,
        $endLatitude,
        $endLongitude,
        $destinationName,
        $destinationAddress,
        $routeDistance,
        $estimatedDuration,
        $transportMode,
        $navigationDuration,
        $routeInstructions,
        $waypointsJson,
        $destinationReached ? 1 : 0,
        $deviceInfo,
        $appVersion,
        $osVersion,
        $additionalDataJson,
        $errorMessage
    ]);
    
    $logId = $conn->lastInsertId();
    
    // Get the created log data
    $logStmt = $conn->prepare("
        SELECT id, user_id, user_name, activity_type, start_latitude, start_longitude, 
               end_latitude, end_longitude, destination_name, destination_address, 
               route_distance, estimated_duration, transport_mode, navigation_duration,
               route_instructions, waypoints, destination_reached, device_info, 
               app_version, os_version, additional_data, error_message, created_at, updated_at
        FROM navigation_activity_logs WHERE id = ?
    ");
    $logStmt->execute([$logId]);
    $log = $logStmt->fetch();
    
    // Parse JSON fields
    if ($log['waypoints']) {
        $log['waypoints'] = json_decode($log['waypoints'], true);
    }
    if ($log['additional_data']) {
        $log['additional_data'] = json_decode($log['additional_data'], true);
    }
    
    // Return success response
    Response::success([
        'log' => $log,
        'log_type' => 'navigation'
    ], 'Navigation log created successfully');
}

/**
 * Create User Activity Log
 */
function createUserActivityLog($conn, $currentUser, $input) {
    // Validate required fields for user activity logs
    $requiredFields = ['log_type_activity', 'action', 'message'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $logTypeActivity = Validator::sanitizeString($input['log_type_activity']);
    $logLevel = isset($input['log_level']) ? Validator::sanitizeString($input['log_level']) : 'info';
    $action = Validator::sanitizeString($input['action']);
    $message = Validator::sanitizeString($input['message']);
    $userAgent = isset($input['user_agent']) ? Validator::sanitizeString($input['user_agent']) : null;
    $additionalData = isset($input['additional_data']) ? $input['additional_data'] : null;
    
    // Validate log level
    $validLogLevels = ['info', 'warning', 'error', 'debug'];
    if (!in_array($logLevel, $validLogLevels)) {
        Response::error('Invalid log level. Must be one of: ' . implode(', ', $validLogLevels));
    }
    
    // Validate JSON fields
    if ($additionalData && !Validator::validateJson(json_encode($additionalData))) {
        Response::error('Invalid additional_data format');
    }
    
    // Insert new user activity log
    $insertStmt = $conn->prepare("
        INSERT INTO user_activity_logs (
            log_type, log_level, user_name, action, message, user_agent, additional_data, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $additionalDataJson = $additionalData ? json_encode($additionalData) : null;
    
    $insertStmt->execute([
        $logTypeActivity,
        $logLevel,
        $currentUser['first_name'] . ' ' . $currentUser['last_name'],
        $action,
        $message,
        $userAgent,
        $additionalDataJson
    ]);
    
    $logId = $conn->lastInsertId();
    
    // Get the created log data
    $logStmt = $conn->prepare("
        SELECT id, log_type, log_level, user_name, action, message, user_agent, additional_data, created_at
        FROM user_activity_logs WHERE id = ?
    ");
    $logStmt->execute([$logId]);
    $log = $logStmt->fetch();
    
    // Parse JSON fields
    if ($log['additional_data']) {
        $log['additional_data'] = json_decode($log['additional_data'], true);
    }
    
    // Return success response
    Response::success([
        'log' => $log,
        'log_type' => 'user_activity'
    ], 'User activity log created successfully');
}
?>

