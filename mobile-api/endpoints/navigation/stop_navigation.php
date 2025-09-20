<?php
/**
 * Stop Navigation Endpoint
 * POST /navigation/stop
 * 
 * Stops an active navigation session and updates the log
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/Validator.php';
require_once '../../includes/SessionManager.php';

try {
    // Check authentication
    SessionManager::requireAuth();
    $currentUser = SessionManager::getUserData();
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input', 400);
    }
    
    // Validate required fields
    $requiredFields = ['log_id'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    $logId = intval($input['log_id']);
    $endLatitude = isset($input['end_latitude']) ? floatval($input['end_latitude']) : null;
    $endLongitude = isset($input['end_longitude']) ? floatval($input['end_longitude']) : null;
    $destinationReached = isset($input['destination_reached']) ? filter_var($input['destination_reached'], FILTER_VALIDATE_BOOLEAN) : false;
    $navigationDuration = isset($input['navigation_duration']) ? max(0, intval($input['navigation_duration'])) : null;
    $actualDistance = isset($input['actual_distance']) ? max(0, floatval($input['actual_distance'])) : null;
    $stopReason = isset($input['stop_reason']) ? Validator::sanitizeString($input['stop_reason']) : 'user_cancelled';
    $additionalData = isset($input['additional_data']) ? $input['additional_data'] : null;
    
    // Validate coordinates if provided
    if ($endLatitude !== null && $endLongitude !== null) {
        if (!Validator::validateCoordinates($endLatitude, $endLongitude)) {
            Response::error('Invalid end coordinates', 400);
        }
    }
    
    // Validate JSON fields
    if ($additionalData && !Validator::validateJson(json_encode($additionalData))) {
        Response::error('Invalid additional_data format', 400);
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Check if log exists and belongs to user
    $checkStmt = $conn->prepare("
        SELECT id, user_id, activity_type, start_latitude, start_longitude,
               destination_name, created_at
        FROM user_navigation_logs 
        WHERE id = ? AND user_id = ?
    ");
    $checkStmt->execute([$logId, $currentUser['id']]);
    $existingLog = $checkStmt->fetch();
    
    if (!$existingLog) {
        Response::error('Navigation log not found or access denied', 404);
    }
    
    // Check if navigation is already stopped
    if ($existingLog['activity_type'] === 'navigation_stop') {
        Response::error('Navigation is already stopped', 400);
    }
    
    // Begin transaction
    $conn->beginTransaction();
    
    try {
        // Update the navigation log
        $updateStmt = $conn->prepare("
            UPDATE user_navigation_logs SET 
                activity_type = 'navigation_stop',
                end_latitude = ?,
                end_longitude = ?,
                destination_reached = ?,
                actual_duration = ?,
                stop_reason = ?,
                additional_data = ?,
                updated_at = NOW()
            WHERE id = ? AND user_id = ?
        ");
        
        $additionalDataJson = $additionalData ? json_encode($additionalData) : null;
        
        $updateStmt->execute([
            $endLatitude,
            $endLongitude,
            $destinationReached ? 1 : 0,
            $navigationDuration,
            $stopReason,
            $additionalDataJson,
            $logId,
            $currentUser['id']
        ]);
        
        // Add navigation stop event
        $eventStmt = $conn->prepare("
            INSERT INTO navigation_events (
                navigation_log_id, event_type, event_data, 
                latitude, longitude, timestamp
            ) VALUES (?, ?, ?, ?, ?, NOW())
        ");
        
        $eventData = [
            'stop_reason' => $stopReason,
            'destination_reached' => $destinationReached,
            'navigation_duration' => $navigationDuration,
            'actual_distance' => $actualDistance
        ];
        
        $eventStmt->execute([
            $logId,
            'navigation_stop',
            json_encode($eventData),
            $endLatitude,
            $endLongitude
        ]);
        
        // If destination was reached, add destination reached event
        if ($destinationReached) {
            $destinationEventStmt = $conn->prepare("
                INSERT INTO navigation_events (
                    navigation_log_id, event_type, event_data, 
                    latitude, longitude, timestamp
                ) VALUES (?, ?, ?, ?, ?, NOW())
            ");
            
            $destinationEventData = [
                'destination_name' => $existingLog['destination_name'],
                'arrival_time' => date('Y-m-d H:i:s'),
                'navigation_duration' => $navigationDuration
            ];
            
            $destinationEventStmt->execute([
                $logId,
                'destination_reached',
                json_encode($destinationEventData),
                $endLatitude,
                $endLongitude
            ]);
        }
        
        // Commit transaction
        $conn->commit();
        
        // Get updated log data
        $logStmt = $conn->prepare("
            SELECT id, user_id, user_name, activity_type, start_latitude, start_longitude, 
                   end_latitude, end_longitude, destination_name, destination_address, 
                   route_distance, estimated_duration, actual_duration, transport_mode,
                   destination_reached, stop_reason, device_model, device_id, battery_level,
                   network_type, gps_accuracy, screen_resolution, available_storage,
                   app_version, os_version, additional_data, error_message, created_at, updated_at
            FROM user_navigation_logs WHERE id = ?
        ");
        $logStmt->execute([$logId]);
        $log = $logStmt->fetch();
        
        // Parse JSON fields
        if ($log['additional_data']) {
            $log['additional_data'] = json_decode($log['additional_data'], true);
        }
        
        // Calculate navigation summary
        $navigationSummary = [
            'log_id' => $logId,
            'start_time' => $existingLog['created_at'],
            'end_time' => $log['updated_at'],
            'duration_minutes' => $navigationDuration,
            'destination_reached' => $destinationReached,
            'stop_reason' => $stopReason,
            'start_location' => [
                'latitude' => $existingLog['start_latitude'],
                'longitude' => $existingLog['start_longitude']
            ],
            'end_location' => [
                'latitude' => $endLatitude,
                'longitude' => $endLongitude
            ]
        ];
        
        // Return success response
        Response::success([
            'log' => $log,
            'navigation_summary' => $navigationSummary,
            'message' => $destinationReached ? 'Navigation completed successfully' : 'Navigation stopped'
        ], 'Navigation stopped successfully');
        
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        throw $e;
    }
    
} catch (Exception $e) {
    Response::serverError('Failed to stop navigation: ' . $e->getMessage());
}
?>

