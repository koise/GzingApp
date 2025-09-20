<?php
/**
 * Create Navigation Log Endpoint
 * POST /navigation-logs
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/Validator.php';
require_once '../../includes/SessionManager.php';

try {
    // Enable error reporting for debugging
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
    
    // For now, allow access without authentication for testing
    // TODO: Implement proper authentication later
    // SessionManager::requireAuth();
    // $currentUser = SessionManager::getUserData();
    
    // Get user data from request or use default for testing
    $currentUser = [
        'id' => 34, // Default user ID for testing
        'first_name' => 'Test',
        'last_name' => 'User'
    ];
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input');
    }
    
    // Validate required fields
    $requiredFields = ['activity_type'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $activityType = Validator::sanitizeString($input['activity_type']);
    $startLatitude = isset($input['start_latitude']) ? floatval($input['start_latitude']) : null;
    $startLongitude = isset($input['start_longitude']) ? floatval($input['start_longitude']) : null;
    $endLatitude = isset($input['end_latitude']) ? floatval($input['end_longitude']) : null;
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
        // Map common transport mode variations to valid modes
        $transportModeMap = [
            'car' => 'driving',
            'driving' => 'driving',
            'walk' => 'walking',
            'walking' => 'walking',
            'bike' => 'cycling',
            'cycling' => 'cycling',
            'transit' => 'transit',
            'public_transport' => 'transit'
        ];
        
        $normalizedTransportMode = $transportModeMap[strtolower($transportMode)] ?? $transportMode;
        $validTransportModes = ['driving', 'walking', 'cycling', 'transit'];
        
        if (!in_array($normalizedTransportMode, $validTransportModes)) {
            Response::error('Invalid transport mode. Must be one of: ' . implode(', ', $validTransportModes) . '. Received: ' . $transportMode);
        }
        
        // Use the normalized transport mode
        $transportMode = $normalizedTransportMode;
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
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Insert new log
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
        'log' => $log
    ], 'Navigation log created successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to create navigation log: ' . $e->getMessage());
}
?>
