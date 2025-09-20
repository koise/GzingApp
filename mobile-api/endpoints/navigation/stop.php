<?php
/**
 * Stop Navigation Endpoint
 * POST /navigation_activity_logs/stop
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
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input');
    }
    
    // Validate required fields - either log_id or user_id
    $logId = null;
    $userId = null;
    
    if (isset($input['log_id'])) {
        $logId = intval($input['log_id']);
    } elseif (isset($input['user_id'])) {
        $userId = intval($input['user_id']);
    } else {
        Response::error('Either log_id or user_id is required');
    }
    $endLatitude = isset($input['end_latitude']) ? floatval($input['end_latitude']) : null;
    $endLongitude = isset($input['end_longitude']) ? floatval($input['end_longitude']) : null;
    $destinationReached = isset($input['destination_reached']) ? filter_var($input['destination_reached'], FILTER_VALIDATE_BOOLEAN) : false;
    $navigationDuration = isset($input['navigation_duration']) ? max(0, intval($input['navigation_duration'])) : null;
    $actualDistance = isset($input['actual_distance']) ? max(0, floatval($input['actual_distance'])) : null;
    $stopReason = isset($input['stop_reason']) ? Validator::sanitizeString($input['stop_reason']) : null;
    $additionalData = isset($input['additional_data']) ? $input['additional_data'] : null;
    
    // Validate coordinates if provided
    if ($endLatitude !== null && $endLongitude !== null) {
        if (!Validator::validateCoordinates($endLatitude, $endLongitude)) {
            Response::error('Invalid end coordinates');
        }
    }
    
    // Validate JSON fields
    if ($additionalData && !Validator::validateJson(json_encode($additionalData))) {
        Response::error('Invalid additional_data format');
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Find the log to update
    $existingLog = null;
    
    if ($logId) {
        // Check if specific log exists
        $checkStmt = $conn->prepare("
            SELECT id, user_id, activity_type, start_latitude, start_longitude, 
                   destination_name, destination_address, transport_mode
            FROM navigation_activity_logs 
            WHERE id = ?
        ");
        $checkStmt->execute([$logId]);
        $existingLog = $checkStmt->fetch();
    } elseif ($userId) {
        // Find the most recent navigation_start log for the user
        $checkStmt = $conn->prepare("
            SELECT id, user_id, activity_type, start_latitude, start_longitude, 
                   destination_name, destination_address, transport_mode
            FROM navigation_activity_logs 
            WHERE user_id = ? AND activity_type = 'navigation_start'
            ORDER BY created_at DESC
            LIMIT 1
        ");
        $checkStmt->execute([$userId]);
        $existingLog = $checkStmt->fetch();
        
        if ($existingLog) {
            $logId = $existingLog['id'];
        }
    }
    
    if (!$existingLog) {
        Response::error('Navigation log not found');
    }
    
    // Update the existing log with stop information
    $updateStmt = $conn->prepare("
        UPDATE navigation_activity_logs 
        SET activity_type = 'navigation_stop',
            end_latitude = COALESCE(?, end_latitude),
            end_longitude = COALESCE(?, end_longitude),
            destination_reached = ?,
            navigation_duration = COALESCE(?, navigation_duration),
            route_distance = COALESCE(?, route_distance),
            additional_data = COALESCE(?, additional_data),
            updated_at = NOW()
        WHERE id = ?
    ");
    
    $additionalDataJson = $additionalData ? json_encode($additionalData) : null;
    
    $updateStmt->execute([
        $endLatitude,
        $endLongitude,
        $destinationReached ? 1 : 0,
        $navigationDuration,
        $actualDistance,
        $additionalDataJson,
        $logId
    ]);
    
    if ($updateStmt->rowCount() === 0) {
        Response::error('Failed to update navigation log');
    }
    
    // Get the updated log data
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
    
    Response::success([
        'log' => $log,
        'stop_info' => [
            'log_id' => $logId,
            'destination_reached' => $destinationReached,
            'navigation_duration' => $navigationDuration,
            'actual_distance' => $actualDistance,
            'stop_reason' => $stopReason,
            'stopped_at' => date('Y-m-d H:i:s')
        ]
    ], 'Navigation stopped successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to stop navigation: ' . $e->getMessage());
}
?>
