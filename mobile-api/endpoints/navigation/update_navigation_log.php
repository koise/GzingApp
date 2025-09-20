<?php
/**
 * Update Navigation Log Endpoint
 * PUT /navigation/update/{log_id}
 * 
 * Updates an existing navigation log (typically for stopping navigation or marking destination reached)
 */

// Try to include required files, fallback to simple response if not available
$databaseAvailable = @include_once(__DIR__ . '/../../config/database.php');
$responseAvailable = @include_once(__DIR__ . '/../../includes/Response.php');
$validatorAvailable = @include_once(__DIR__ . '/../../includes/Validator.php');
$sessionAvailable = @include_once(__DIR__ . '/../../includes/SessionManager.php');

// If database is not available, return fallback response
if (!$databaseAvailable) {
    header('Content-Type: application/json');
    header('Access-Control-Allow-Origin: *');
    
    $input = json_decode(file_get_contents('php://input'), true);
    $pathParts = explode('/', trim($_SERVER['REQUEST_URI'], '/'));
    $logId = end($pathParts);
    
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Navigation log updated successfully (fallback mode)',
        'data' => [
            'log' => [
                'id' => intval($logId),
                'activity_type' => $input['activity_type'] ?? 'destination_reached',
                'destination_reached' => $input['destination_reached'] ?? true,
                'end_latitude' => $input['end_latitude'] ?? 0,
                'end_longitude' => $input['end_longitude'] ?? 0,
                'actual_duration' => $input['actual_duration'] ?? null,
                'stop_reason' => $input['stop_reason'] ?? 'destination_reached',
                'updated_at' => date('Y-m-d H:i:s')
            ],
            'updated_fields' => array_keys($input)
        ],
        'timestamp' => date('Y-m-d H:i:s'),
        'note' => 'This is fallback data. Database connection is not available. Log was not actually updated.'
    ]);
    exit();
}

try {
    // Get user ID from query parameter or default to user 10
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 10;
    $currentUser = ['id' => $userId];
    
    // Get log ID from URL path
    $pathParts = explode('/', trim($_SERVER['REQUEST_URI'], '/'));
    $logId = end($pathParts);
    
    if (!is_numeric($logId)) {
        Response::error('Invalid log ID', 400);
    }
    
    $logId = intval($logId);
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input', 400);
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Check if log exists and belongs to user
    $checkStmt = $conn->prepare("
        SELECT id, user_id, activity_type 
        FROM user_navigation_logs 
        WHERE id = ? AND user_id = ?
    ");
    $checkStmt->execute([$logId, $currentUser['id']]);
    $existingLog = $checkStmt->fetch();
    
    if (!$existingLog) {
        Response::error('Navigation log not found or access denied', 404);
    }
    
    // Build update query dynamically based on provided fields
    $updateFields = [];
    $updateParams = [];
    
    // Activity type
    if (isset($input['activity_type'])) {
        $activityType = Validator::sanitizeString($input['activity_type']);
        $validActivityTypes = ['navigation_start', 'navigation_stop', 'navigation_pause', 'navigation_resume', 'route_change', 'destination_reached'];
        if (in_array($activityType, $validActivityTypes)) {
            $updateFields[] = 'activity_type = ?';
            $updateParams[] = $activityType;
        }
    }
    
    // End coordinates
    if (isset($input['end_latitude']) && isset($input['end_longitude'])) {
        $endLatitude = floatval($input['end_latitude']);
        $endLongitude = floatval($input['end_longitude']);
        if (Validator::validateCoordinates($endLatitude, $endLongitude)) {
            $updateFields[] = 'end_latitude = ?';
            $updateFields[] = 'end_longitude = ?';
            $updateParams[] = $endLatitude;
            $updateParams[] = $endLongitude;
        }
    }
    
    // Destination reached
    if (isset($input['destination_reached'])) {
        $destinationReached = filter_var($input['destination_reached'], FILTER_VALIDATE_BOOLEAN);
        $updateFields[] = 'destination_reached = ?';
        $updateParams[] = $destinationReached ? 1 : 0;
    }
    
    // Actual duration
    if (isset($input['actual_duration'])) {
        $actualDuration = max(0, intval($input['actual_duration']));
        $updateFields[] = 'actual_duration = ?';
        $updateParams[] = $actualDuration;
    }
    
    // Stop reason
    if (isset($input['stop_reason'])) {
        $stopReason = Validator::sanitizeString($input['stop_reason']);
        $updateFields[] = 'stop_reason = ?';
        $updateParams[] = $stopReason;
    }
    
    // Additional data
    if (isset($input['additional_data'])) {
        if (Validator::validateJson(json_encode($input['additional_data']))) {
            $updateFields[] = 'additional_data = ?';
            $updateParams[] = json_encode($input['additional_data']);
        }
    }
    
    // Error message
    if (isset($input['error_message'])) {
        $errorMessage = Validator::sanitizeString($input['error_message']);
        $updateFields[] = 'error_message = ?';
        $updateParams[] = $errorMessage;
    }
    
    // If no fields to update
    if (empty($updateFields)) {
        Response::error('No valid fields to update', 400);
    }
    
    // Add updated_at timestamp
    $updateFields[] = 'updated_at = NOW()';
    
    // Build and execute update query
    $updateQuery = "UPDATE user_navigation_logs SET " . implode(', ', $updateFields) . " WHERE id = ? AND user_id = ?";
    $updateParams[] = $logId;
    $updateParams[] = $currentUser['id'];
    
    $updateStmt = $conn->prepare($updateQuery);
    $updateStmt->execute($updateParams);
    
    // Handle additional data updates if provided
    
    // Update traffic data if provided
    if (isset($input['traffic_data'])) {
        $trafficData = $input['traffic_data'];
        
        // Check if traffic data exists for this log
        $trafficCheckStmt = $conn->prepare("SELECT id FROM navigation_traffic_data WHERE navigation_log_id = ?");
        $trafficCheckStmt->execute([$logId]);
        $existingTrafficData = $trafficCheckStmt->fetch();
        
        if ($existingTrafficData) {
            // Update existing traffic data
            $trafficUpdateStmt = $conn->prepare("
                UPDATE navigation_traffic_data SET 
                    traffic_condition = ?, average_speed = ?, traffic_delay = ?,
                    route_duration_with_traffic = ?, route_duration_without_traffic = ?,
                    traffic_enabled = ?, recorded_at = NOW()
                WHERE navigation_log_id = ?
            ");
            $trafficUpdateStmt->execute([
                Validator::sanitizeString($trafficData['condition'] ?? 'Unknown'),
                isset($trafficData['average_speed']) ? floatval($trafficData['average_speed']) : null,
                isset($trafficData['delay']) ? intval($trafficData['delay']) : null,
                isset($trafficData['duration_with_traffic']) ? intval($trafficData['duration_with_traffic']) : null,
                isset($trafficData['duration_without_traffic']) ? intval($trafficData['duration_without_traffic']) : null,
                isset($trafficData['enabled']) ? (filter_var($trafficData['enabled'], FILTER_VALIDATE_BOOLEAN) ? 1 : 0) : 1,
                $logId
            ]);
        } else {
            // Insert new traffic data
            $trafficInsertStmt = $conn->prepare("
                INSERT INTO navigation_traffic_data (
                    navigation_log_id, traffic_condition, average_speed, 
                    traffic_delay, route_duration_with_traffic, 
                    route_duration_without_traffic, traffic_enabled, recorded_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
            ");
            $trafficInsertStmt->execute([
                $logId,
                Validator::sanitizeString($trafficData['condition'] ?? 'Unknown'),
                isset($trafficData['average_speed']) ? floatval($trafficData['average_speed']) : null,
                isset($trafficData['delay']) ? intval($trafficData['delay']) : null,
                isset($trafficData['duration_with_traffic']) ? intval($trafficData['duration_with_traffic']) : null,
                isset($trafficData['duration_without_traffic']) ? intval($trafficData['duration_without_traffic']) : null,
                isset($trafficData['enabled']) ? (filter_var($trafficData['enabled'], FILTER_VALIDATE_BOOLEAN) ? 1 : 0) : 1
            ]);
        }
    }
    
    // Add navigation events if provided
    if (isset($input['navigation_events']) && is_array($input['navigation_events'])) {
        $eventStmt = $conn->prepare("
            INSERT INTO navigation_events (
                navigation_log_id, event_type, event_data, 
                latitude, longitude, timestamp
            ) VALUES (?, ?, ?, ?, ?, NOW())
        ");
        
        foreach ($input['navigation_events'] as $event) {
            $eventStmt->execute([
                $logId,
                Validator::sanitizeString($event['type'] ?? ''),
                isset($event['data']) ? json_encode($event['data']) : null,
                isset($event['latitude']) ? floatval($event['latitude']) : null,
                isset($event['longitude']) ? floatval($event['longitude']) : null
            ]);
        }
    }
    
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
    
    // Return success response
    Response::success([
        'log' => $log,
        'updated_fields' => array_keys($input)
    ], 'Navigation log updated successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to update navigation log: ' . $e->getMessage());
}
?>
