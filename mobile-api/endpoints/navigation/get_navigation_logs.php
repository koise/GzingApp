<?php
/**
 * Get Navigation Logs Endpoint
 * GET /navigation/logs
 * 
 * Retrieves navigation logs for the authenticated user with filtering and pagination
 */

// Try to include required files, fallback to simple response if not available
$databaseAvailable = @include_once(__DIR__ . '/../../config/database.php');
$responseAvailable = @include_once(__DIR__ . '/../../includes/Response.php');
$validatorAvailable = @include_once(__DIR__ . '/../../includes/Validator.php');
$sessionAvailable = @include_once(__DIR__ . '/../../includes/SessionManager.php');

// If database is not available, return fallback data
if (!$databaseAvailable) {
    header('Content-Type: application/json');
    header('Access-Control-Allow-Origin: *');
    
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 10;
    $limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 100;
    
    $fallbackLogs = [
        [
            'id' => 1,
            'user_id' => $userId,
            'activity_type' => 'navigation_start',
            'start_latitude' => 14.5995,
            'start_longitude' => 120.9842,
            'destination_name' => 'Luneta Park',
            'destination_address' => 'Luneta Park, Manila',
            'route_distance' => 12.5,
            'estimated_duration' => 25,
            'transport_mode' => 'driving',
            'status' => 'completed',
            'destination_reached' => true,
            'created_at' => '2024-01-15 10:30:00',
            'updated_at' => '2024-01-15 10:55:00'
        ],
        [
            'id' => 2,
            'user_id' => $userId,
            'activity_type' => 'navigation_start',
            'start_latitude' => 14.6500,
            'start_longitude' => 121.0700,
            'destination_name' => 'La Salle Greenhills',
            'destination_address' => 'La Salle Greenhills, San Juan',
            'route_distance' => 8.3,
            'estimated_duration' => 18,
            'transport_mode' => 'driving',
            'status' => 'completed',
            'destination_reached' => true,
            'created_at' => '2024-01-14 14:20:00',
            'updated_at' => '2024-01-14 14:38:00'
        ]
    ];
    
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Navigation logs retrieved successfully (fallback data)',
        'data' => [
            'logs' => $fallbackLogs,
            'pagination' => [
                'current_page' => 1,
                'total_pages' => 1,
                'total_items' => count($fallbackLogs),
                'items_per_page' => $limit,
                'has_next' => false,
                'has_prev' => false
            ]
        ],
        'timestamp' => date('Y-m-d H:i:s'),
        'note' => 'This is fallback data. Database connection is not available.'
    ]);
    exit();
}

try {
    // Get user ID from query parameter instead of authentication
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 10; // Default to user 10
    $currentUser = ['id' => $userId];
    
    // Get query parameters
    $page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
    $limit = isset($_GET['limit']) ? max(1, min(100, intval($_GET['limit']))) : 20;
    $activityType = isset($_GET['activity_type']) ? Validator::sanitizeString($_GET['activity_type']) : null;
    $transportMode = isset($_GET['transport_mode']) ? Validator::sanitizeString($_GET['transport_mode']) : null;
    $destinationReached = isset($_GET['destination_reached']) ? filter_var($_GET['destination_reached'], FILTER_VALIDATE_BOOLEAN) : null;
    $dateFrom = isset($_GET['date_from']) ? Validator::sanitizeString($_GET['date_from']) : null;
    $dateTo = isset($_GET['date_to']) ? Validator::sanitizeString($_GET['date_to']) : null;
    $includeDetails = isset($_GET['include_details']) ? filter_var($_GET['include_details'], FILTER_VALIDATE_BOOLEAN) : false;
    
    // Calculate offset
    $offset = ($page - 1) * $limit;
    
    // Try to initialize database, fallback to sample data if it fails
    try {
        $db = new Database();
        $conn = $db->getConnection();
    } catch (Exception $dbError) {
        // If database connection fails, use fallback data
        require_once __DIR__ . '/get_navigation_logs_fallback.php';
        exit();
    }
    
    // Build WHERE clause
    $whereConditions = ['user_id = ?'];
    $params = [$currentUser['id']];
    
    if ($activityType) {
        $whereConditions[] = 'activity_type = ?';
        $params[] = $activityType;
    }
    
    if ($transportMode) {
        $whereConditions[] = 'transport_mode = ?';
        $params[] = $transportMode;
    }
    
    if ($destinationReached !== null) {
        $whereConditions[] = 'destination_reached = ?';
        $params[] = $destinationReached ? 1 : 0;
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
    
    // Get total count
    $countStmt = $conn->prepare("SELECT COUNT(*) as total FROM user_navigation_logs WHERE $whereClause");
    $countStmt->execute($params);
    $totalCount = $countStmt->fetch()['total'];
    
    // Get logs with pagination
    $logsStmt = $conn->prepare("
        SELECT id, user_id, user_name, activity_type, start_latitude, start_longitude, 
               end_latitude, end_longitude, destination_name, destination_address, 
               route_distance, estimated_duration, actual_duration, transport_mode,
               destination_reached, stop_reason, device_model, device_id, battery_level,
               network_type, gps_accuracy, screen_resolution, available_storage,
               app_version, os_version, additional_data, error_message, created_at, updated_at
        FROM user_navigation_logs 
        WHERE $whereClause
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?
    ");
    
    $logsParams = array_merge($params, [$limit, $offset]);
    $logsStmt->execute($logsParams);
    $logs = $logsStmt->fetchAll();
    
    // Parse JSON fields and add additional details if requested
    foreach ($logs as &$log) {
        if ($log['additional_data']) {
            $log['additional_data'] = json_decode($log['additional_data'], true);
        }
        
        if ($includeDetails) {
            // Get route instructions
            $instructionsStmt = $conn->prepare("
                SELECT instruction_order, instruction_text, distance, duration, maneuver_type
                FROM navigation_route_instructions 
                WHERE navigation_log_id = ? 
                ORDER BY instruction_order
            ");
            $instructionsStmt->execute([$log['id']]);
            $log['route_instructions'] = $instructionsStmt->fetchAll();
            
            // Get waypoints
            $waypointsStmt = $conn->prepare("
                SELECT waypoint_order, latitude, longitude, name, type, address
                FROM navigation_waypoints 
                WHERE navigation_log_id = ? 
                ORDER BY waypoint_order
            ");
            $waypointsStmt->execute([$log['id']]);
            $log['waypoints'] = $waypointsStmt->fetchAll();
            
            // Get route polylines
            $polylinesStmt = $conn->prepare("
                SELECT polyline_type, polyline_data, color, width, opacity
                FROM navigation_route_polylines 
                WHERE navigation_log_id = ?
            ");
            $polylinesStmt->execute([$log['id']]);
            $log['route_polylines'] = $polylinesStmt->fetchAll();
            
            // Get traffic data
            $trafficStmt = $conn->prepare("
                SELECT traffic_condition, average_speed, traffic_delay, 
                       route_duration_with_traffic, route_duration_without_traffic, 
                       traffic_enabled, recorded_at
                FROM navigation_traffic_data 
                WHERE navigation_log_id = ?
            ");
            $trafficStmt->execute([$log['id']]);
            $log['traffic_data'] = $trafficStmt->fetchAll();
            
            // Get navigation events
            $eventsStmt = $conn->prepare("
                SELECT event_type, event_data, latitude, longitude, timestamp
                FROM navigation_events 
                WHERE navigation_log_id = ? 
                ORDER BY timestamp
            ");
            $eventsStmt->execute([$log['id']]);
            $events = $eventsStmt->fetchAll();
            
            // Parse event data JSON
            foreach ($events as &$event) {
                if ($event['event_data']) {
                    $event['event_data'] = json_decode($event['event_data'], true);
                }
            }
            $log['navigation_events'] = $events;
        }
    }
    
    // Calculate pagination info
    $totalPages = ceil($totalCount / $limit);
    $hasNextPage = $page < $totalPages;
    $hasPrevPage = $page > 1;
    
    // Return success response
    Response::success([
        'logs' => $logs,
        'pagination' => [
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_count' => $totalCount,
            'limit' => $limit,
            'has_next_page' => $hasNextPage,
            'has_prev_page' => $hasPrevPage
        ]
    ], 'Navigation logs retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve navigation logs: ' . $e->getMessage());
}
?>
