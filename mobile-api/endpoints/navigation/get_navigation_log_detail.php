<?php
/**
 * Get Navigation Log Detail Endpoint
 * GET /navigation/log/{log_id}
 * 
 * Retrieves detailed information for a specific navigation log
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/Validator.php';
require_once '../../includes/SessionManager.php';

try {
    // Check authentication
    SessionManager::requireAuth();
    $currentUser = SessionManager::getUserData();
    
    // Get log ID from URL path
    $pathParts = explode('/', trim($_SERVER['REQUEST_URI'], '/'));
    $logId = end($pathParts);
    
    if (!is_numeric($logId)) {
        Response::error('Invalid log ID', 400);
    }
    
    $logId = intval($logId);
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get main navigation log
    $logStmt = $conn->prepare("
        SELECT id, user_id, user_name, activity_type, start_latitude, start_longitude, 
               end_latitude, end_longitude, destination_name, destination_address, 
               route_distance, estimated_duration, actual_duration, transport_mode,
               destination_reached, stop_reason, device_model, device_id, battery_level,
               network_type, gps_accuracy, screen_resolution, available_storage,
               app_version, os_version, additional_data, error_message, created_at, updated_at
        FROM user_navigation_logs 
        WHERE id = ? AND user_id = ?
    ");
    $logStmt->execute([$logId, $currentUser['id']]);
    $log = $logStmt->fetch();
    
    if (!$log) {
        Response::error('Navigation log not found or access denied', 404);
    }
    
    // Parse JSON fields
    if ($log['additional_data']) {
        $log['additional_data'] = json_decode($log['additional_data'], true);
    }
    
    // Get route instructions
    $instructionsStmt = $conn->prepare("
        SELECT instruction_order, instruction_text, distance, duration, maneuver_type
        FROM navigation_route_instructions 
        WHERE navigation_log_id = ? 
        ORDER BY instruction_order
    ");
    $instructionsStmt->execute([$logId]);
    $routeInstructions = $instructionsStmt->fetchAll();
    
    // Get waypoints
    $waypointsStmt = $conn->prepare("
        SELECT waypoint_order, latitude, longitude, name, type, address
        FROM navigation_waypoints 
        WHERE navigation_log_id = ? 
        ORDER BY waypoint_order
    ");
    $waypointsStmt->execute([$logId]);
    $waypoints = $waypointsStmt->fetchAll();
    
    // Get route polylines
    $polylinesStmt = $conn->prepare("
        SELECT polyline_type, polyline_data, color, width, opacity
        FROM navigation_route_polylines 
        WHERE navigation_log_id = ?
    ");
    $polylinesStmt->execute([$logId]);
    $routePolylines = $polylinesStmt->fetchAll();
    
    // Get traffic data
    $trafficStmt = $conn->prepare("
        SELECT traffic_condition, average_speed, traffic_delay, 
               route_duration_with_traffic, route_duration_without_traffic, 
               traffic_enabled, recorded_at
        FROM navigation_traffic_data 
        WHERE navigation_log_id = ?
    ");
    $trafficStmt->execute([$logId]);
    $trafficData = $trafficStmt->fetchAll();
    
    // Get navigation events
    $eventsStmt = $conn->prepare("
        SELECT event_type, event_data, latitude, longitude, timestamp
        FROM navigation_events 
        WHERE navigation_log_id = ? 
        ORDER BY timestamp
    ");
    $eventsStmt->execute([$logId]);
    $events = $eventsStmt->fetchAll();
    
    // Parse event data JSON
    foreach ($events as &$event) {
        if ($event['event_data']) {
            $event['event_data'] = json_decode($event['event_data'], true);
        }
    }
    
    // Calculate additional metrics
    $navigationMetrics = [
        'duration_accuracy' => null,
        'distance_accuracy' => null,
        'success_rate' => null,
        'efficiency_score' => null
    ];
    
    if ($log['estimated_duration'] && $log['actual_duration']) {
        $durationAccuracy = round((1 - abs($log['estimated_duration'] - $log['actual_duration']) / $log['estimated_duration']) * 100, 2);
        $navigationMetrics['duration_accuracy'] = max(0, $durationAccuracy);
    }
    
    if ($log['route_distance'] && isset($log['additional_data']['actual_distance'])) {
        $actualDistance = floatval($log['additional_data']['actual_distance']);
        $distanceAccuracy = round((1 - abs($log['route_distance'] - $actualDistance) / $log['route_distance']) * 100, 2);
        $navigationMetrics['distance_accuracy'] = max(0, $distanceAccuracy);
    }
    
    if ($log['activity_type'] === 'navigation_stop') {
        $navigationMetrics['success_rate'] = $log['destination_reached'] ? 100 : 0;
    }
    
    // Calculate efficiency score based on multiple factors
    $efficiencyFactors = [];
    if ($navigationMetrics['duration_accuracy'] !== null) {
        $efficiencyFactors[] = $navigationMetrics['duration_accuracy'];
    }
    if ($navigationMetrics['distance_accuracy'] !== null) {
        $efficiencyFactors[] = $navigationMetrics['distance_accuracy'];
    }
    if ($navigationMetrics['success_rate'] !== null) {
        $efficiencyFactors[] = $navigationMetrics['success_rate'];
    }
    
    if (!empty($efficiencyFactors)) {
        $navigationMetrics['efficiency_score'] = round(array_sum($efficiencyFactors) / count($efficiencyFactors), 2);
    }
    
    // Prepare response data
    $responseData = [
        'log' => $log,
        'route_instructions' => $routeInstructions,
        'waypoints' => $waypoints,
        'route_polylines' => $routePolylines,
        'traffic_data' => $trafficData,
        'navigation_events' => $events,
        'navigation_metrics' => $navigationMetrics,
        'summary' => [
            'total_instructions' => count($routeInstructions),
            'total_waypoints' => count($waypoints),
            'total_polylines' => count($routePolylines),
            'total_events' => count($events),
            'has_traffic_data' => !empty($trafficData),
            'navigation_completed' => $log['activity_type'] === 'navigation_stop',
            'destination_reached' => $log['destination_reached']
        ]
    ];
    
    // Return success response
    Response::success($responseData, 'Navigation log details retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve navigation log details: ' . $e->getMessage());
}
?>

