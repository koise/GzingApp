<?php
/**
 * Create Navigation Log Endpoint
 * POST /navigation/create
 * 
 * Creates a new navigation log using the new user_navigation_logs table structure
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
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Invalid JSON input',
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    // Generate a mock log ID
    $mockLogId = rand(1000, 9999);
    
    // Return success response with mock data
    http_response_code(201);
    echo json_encode([
        'success' => true,
        'message' => 'Navigation log created successfully (fallback mode)',
        'data' => [
            'log_id' => $mockLogId,
            'activity_type' => $input['activity_type'] ?? 'navigation_start',
            'start_latitude' => $input['start_latitude'] ?? 0,
            'start_longitude' => $input['start_longitude'] ?? 0,
            'destination_name' => $input['destination_name'] ?? 'Unknown',
            'destination_address' => $input['destination_address'] ?? null,
            'route_distance' => $input['route_distance'] ?? 0,
            'estimated_duration' => $input['estimated_duration'] ?? 0,
            'transport_mode' => $input['transport_mode'] ?? 'driving',
            'status' => 'created',
            'created_at' => date('Y-m-d H:i:s'),
            'updated_at' => date('Y-m-d H:i:s')
        ],
        'timestamp' => date('Y-m-d H:i:s'),
        'note' => 'This is fallback data. Database connection is not available. Log was not actually saved.'
    ]);
    exit();
}

try {
    // Enable error reporting for debugging
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
    
    // Get user ID from query parameter or default to user 10
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 10;
    $currentUser = ['id' => $userId];
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input', 400);
    }
    
    // Validate required fields
    $requiredFields = ['activity_type'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize and validate input
    $activityType = Validator::sanitizeString($input['activity_type']);
    $startLatitude = isset($input['start_latitude']) ? floatval($input['start_latitude']) : null;
    $startLongitude = isset($input['start_longitude']) ? floatval($input['start_longitude']) : null;
    $endLatitude = isset($input['end_latitude']) ? floatval($input['end_latitude']) : null;
    $endLongitude = isset($input['end_longitude']) ? floatval($input['end_longitude']) : null;
    $destinationName = isset($input['destination_name']) ? Validator::sanitizeString($input['destination_name']) : null;
    $destinationAddress = isset($input['destination_address']) ? Validator::sanitizeString($input['destination_address']) : null;
    $routeDistance = isset($input['route_distance']) ? max(0, floatval($input['route_distance'])) : null;
    $estimatedDuration = isset($input['estimated_duration']) ? max(0, intval($input['estimated_duration'])) : null;
    $actualDuration = isset($input['actual_duration']) ? max(0, intval($input['actual_duration'])) : null;
    $transportMode = isset($input['transport_mode']) ? Validator::sanitizeString($input['transport_mode']) : null;
    $destinationReached = isset($input['destination_reached']) ? filter_var($input['destination_reached'], FILTER_VALIDATE_BOOLEAN) : false;
    $stopReason = isset($input['stop_reason']) ? Validator::sanitizeString($input['stop_reason']) : null;
    
    // Device information
    $deviceModel = isset($input['device_model']) ? Validator::sanitizeString($input['device_model']) : null;
    $deviceId = isset($input['device_id']) ? Validator::sanitizeString($input['device_id']) : null;
    $batteryLevel = isset($input['battery_level']) ? max(0, min(100, intval($input['battery_level']))) : null;
    $networkType = isset($input['network_type']) ? Validator::sanitizeString($input['network_type']) : null;
    $gpsAccuracy = isset($input['gps_accuracy']) ? Validator::sanitizeString($input['gps_accuracy']) : null;
    $screenResolution = isset($input['screen_resolution']) ? Validator::sanitizeString($input['screen_resolution']) : null;
    $availableStorage = isset($input['available_storage']) ? max(0, intval($input['available_storage'])) : null;
    $appVersion = isset($input['app_version']) ? Validator::sanitizeString($input['app_version']) : null;
    $osVersion = isset($input['os_version']) ? Validator::sanitizeString($input['os_version']) : null;
    $additionalData = isset($input['additional_data']) ? $input['additional_data'] : null;
    $errorMessage = isset($input['error_message']) ? Validator::sanitizeString($input['error_message']) : null;
    
    // Validate activity type
    $validActivityTypes = ['navigation_start', 'navigation_stop', 'navigation_pause', 'navigation_resume', 'route_change', 'destination_reached'];
    if (!in_array($activityType, $validActivityTypes)) {
        Response::error('Invalid activity type. Must be one of: ' . implode(', ', $validActivityTypes), 400);
    }
    
    // Validate transport mode if provided
    if ($transportMode) {
        $validTransportModes = ['driving', 'walking', 'cycling', 'transit', 'car', 'walk', 'motor'];
        if (!in_array(strtolower($transportMode), $validTransportModes)) {
            Response::error('Invalid transport mode. Must be one of: ' . implode(', ', $validTransportModes), 400);
        }
    }
    
    // Validate network type if provided
    if ($networkType) {
        $validNetworkTypes = ['WiFi', 'Mobile', 'Unknown'];
        if (!in_array($networkType, $validNetworkTypes)) {
            Response::error('Invalid network type. Must be one of: ' . implode(', ', $validNetworkTypes), 400);
        }
    }
    
    // Validate coordinates if provided
    if ($startLatitude !== null && $startLongitude !== null) {
        if (!Validator::validateCoordinates($startLatitude, $startLongitude)) {
            Response::error('Invalid start coordinates', 400);
        }
    }
    
    if ($endLatitude !== null && $endLongitude !== null) {
        if (!Validator::validateCoordinates($endLatitude, $endLongitude)) {
            Response::error('Invalid end coordinates', 400);
        }
    }
    
    // Validate JSON fields
    if ($additionalData && !Validator::validateJson(json_encode($additionalData))) {
        Response::error('Invalid additional_data format', 400);
    }
    
    // Try to initialize database, fallback to sample data if it fails
    try {
        $db = new Database();
        $conn = $db->getConnection();
    } catch (Exception $dbError) {
        // If database connection fails, use fallback data
        require_once __DIR__ . '/create_navigation_log_fallback.php';
        exit();
    }
    
    // Begin transaction
    $conn->beginTransaction();
    
    try {
        // Insert main navigation log
        $insertStmt = $conn->prepare("
            INSERT INTO user_navigation_logs (
                user_id, user_name, activity_type, start_latitude, start_longitude, 
                end_latitude, end_longitude, destination_name, destination_address, 
                route_distance, estimated_duration, actual_duration, transport_mode,
                destination_reached, stop_reason, device_model, device_id, battery_level,
                network_type, gps_accuracy, screen_resolution, available_storage,
                app_version, os_version, additional_data, error_message, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        ");
        
        $additionalDataJson = $additionalData ? json_encode($additionalData) : null;
        
        $insertStmt->execute([
            $currentUser['id'],
            ($currentUser['first_name'] ?? '') . ' ' . ($currentUser['last_name'] ?? ''),
            $activityType,
            $startLatitude,
            $startLongitude,
            $endLatitude,
            $endLongitude,
            $destinationName,
            $destinationAddress,
            $routeDistance,
            $estimatedDuration,
            $actualDuration,
            $transportMode,
            $destinationReached ? 1 : 0,
            $stopReason,
            $deviceModel,
            $deviceId,
            $batteryLevel,
            $networkType,
            $gpsAccuracy,
            $screenResolution,
            $availableStorage,
            $appVersion,
            $osVersion,
            $additionalDataJson,
            $errorMessage
        ]);
        
        $logId = $conn->lastInsertId();
        
        // Handle route instructions if provided
        if (isset($input['route_instructions']) && is_array($input['route_instructions'])) {
            $instructionStmt = $conn->prepare("
                INSERT INTO navigation_route_instructions (
                    navigation_log_id, instruction_order, instruction_text, 
                    distance, duration, maneuver_type, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, NOW())
            ");
            
            foreach ($input['route_instructions'] as $index => $instruction) {
                $instructionStmt->execute([
                    $logId,
                    $index + 1,
                    Validator::sanitizeString($instruction['instruction'] ?? ''),
                    isset($instruction['distance']) ? floatval($instruction['distance']) : null,
                    isset($instruction['duration']) ? intval($instruction['duration']) : null,
                    isset($instruction['maneuver']) ? Validator::sanitizeString($instruction['maneuver']) : null
                ]);
            }
        }
        
        // Handle waypoints if provided
        if (isset($input['waypoints']) && is_array($input['waypoints'])) {
            $waypointStmt = $conn->prepare("
                INSERT INTO navigation_waypoints (
                    navigation_log_id, waypoint_order, latitude, longitude, 
                    name, type, address, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
            ");
            
            foreach ($input['waypoints'] as $index => $waypoint) {
                $waypointStmt->execute([
                    $logId,
                    $index + 1,
                    floatval($waypoint['lat'] ?? $waypoint['latitude'] ?? 0),
                    floatval($waypoint['lng'] ?? $waypoint['longitude'] ?? 0),
                    isset($waypoint['name']) ? Validator::sanitizeString($waypoint['name']) : null,
                    isset($waypoint['type']) ? Validator::sanitizeString($waypoint['type']) : null,
                    isset($waypoint['address']) ? Validator::sanitizeString($waypoint['address']) : null
                ]);
            }
        }
        
        // Handle route polylines if provided
        if (isset($input['route_polylines']) && is_array($input['route_polylines'])) {
            $polylineStmt = $conn->prepare("
                INSERT INTO navigation_route_polylines (
                    navigation_log_id, polyline_type, polyline_data, 
                    color, width, opacity, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, NOW())
            ");
            
            foreach ($input['route_polylines'] as $polyline) {
                $polylineStmt->execute([
                    $logId,
                    Validator::sanitizeString($polyline['type'] ?? 'api_response'),
                    Validator::sanitizeString($polyline['data'] ?? ''),
                    isset($polyline['color']) ? Validator::sanitizeString($polyline['color']) : null,
                    isset($polyline['width']) ? floatval($polyline['width']) : null,
                    isset($polyline['opacity']) ? floatval($polyline['opacity']) : null
                ]);
            }
        }
        
        // Handle traffic data if provided
        if (isset($input['traffic_data'])) {
            $trafficData = $input['traffic_data'];
            $trafficStmt = $conn->prepare("
                INSERT INTO navigation_traffic_data (
                    navigation_log_id, traffic_condition, average_speed, 
                    traffic_delay, route_duration_with_traffic, 
                    route_duration_without_traffic, traffic_enabled, recorded_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
            ");
            
            $trafficStmt->execute([
                $logId,
                Validator::sanitizeString($trafficData['condition'] ?? 'Unknown'),
                isset($trafficData['average_speed']) ? floatval($trafficData['average_speed']) : null,
                isset($trafficData['delay']) ? intval($trafficData['delay']) : null,
                isset($trafficData['duration_with_traffic']) ? intval($trafficData['duration_with_traffic']) : null,
                isset($trafficData['duration_without_traffic']) ? intval($trafficData['duration_without_traffic']) : null,
                isset($trafficData['enabled']) ? (filter_var($trafficData['enabled'], FILTER_VALIDATE_BOOLEAN) ? 1 : 0) : 1
            ]);
        }
        
        // Handle navigation events if provided
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
        
        // Commit transaction
        $conn->commit();
        
        // Get the created log data
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
            'log_id' => $logId
        ], 'Navigation log created successfully');
        
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        throw $e;
    }
    
} catch (Exception $e) {
    Response::serverError('Failed to create navigation log: ' . $e->getMessage());
}
?>
