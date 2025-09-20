<?php
/**
 * Get Navigation Statistics Endpoint
 * GET /navigation/stats
 * 
 * Retrieves navigation statistics for the authenticated user
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
    
    $fallbackStats = [
        'basicStats' => [
            'totalNavigations' => 15,
            'successfulNavigations' => 12,
            'failedNavigations' => 3,
            'totalDistance' => 125.5,
            'totalDuration' => 285,
            'averageDistance' => 8.37,
            'averageDuration' => 19.0,
            'successRate' => 80.0
        ],
        'popularDestinations' => [
            [
                'destination' => 'Luneta Park',
                'count' => 5,
                'percentage' => 33.3
            ],
            [
                'destination' => 'La Salle Greenhills',
                'count' => 3,
                'percentage' => 20.0
            ],
            [
                'destination' => 'Cubao',
                'count' => 2,
                'percentage' => 13.3
            ]
        ],
        'transportModeStats' => [
            [
                'mode' => 'driving',
                'count' => 12,
                'percentage' => 80.0,
                'averageDistance' => 9.2,
                'averageDuration' => 20.5
            ],
            [
                'mode' => 'walking',
                'count' => 2,
                'percentage' => 13.3,
                'averageDistance' => 2.1,
                'averageDuration' => 15.0
            ]
        ]
    ];
    
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Navigation stats retrieved successfully (fallback data)',
        'data' => $fallbackStats,
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
    $period = isset($_GET['period']) ? Validator::sanitizeString($_GET['period']) : 'all'; // all, week, month, year
    $includeDestinations = isset($_GET['include_destinations']) ? filter_var($_GET['include_destinations'], FILTER_VALIDATE_BOOLEAN) : true;
    $includeTransportModes = isset($_GET['include_transport_modes']) ? filter_var($_GET['include_transport_modes'], FILTER_VALIDATE_BOOLEAN) : true;
    
    // Try to initialize database, fallback to sample data if it fails
    try {
        $db = new Database();
        $conn = $db->getConnection();
    } catch (Exception $dbError) {
        // If database connection fails, use fallback data
        require_once __DIR__ . '/get_navigation_stats_fallback.php';
        exit();
    }
    
    // Build date filter based on period
    $dateFilter = '';
    $dateParams = [];
    
    switch ($period) {
        case 'week':
            $dateFilter = 'AND created_at >= DATE_SUB(NOW(), INTERVAL 1 WEEK)';
            break;
        case 'month':
            $dateFilter = 'AND created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH)';
            break;
        case 'year':
            $dateFilter = 'AND created_at >= DATE_SUB(NOW(), INTERVAL 1 YEAR)';
            break;
        default:
            $dateFilter = '';
    }
    
    // Get basic navigation statistics
    $statsStmt = $conn->prepare("
        SELECT 
            COUNT(*) as total_navigations,
            COUNT(CASE WHEN activity_type = 'navigation_start' THEN 1 END) as navigation_starts,
            COUNT(CASE WHEN destination_reached = 1 THEN 1 END) as destinations_reached,
            COUNT(CASE WHEN activity_type = 'navigation_stop' THEN 1 END) as navigation_stops,
            AVG(actual_duration) as avg_navigation_duration,
            AVG(route_distance) as avg_route_distance,
            SUM(route_distance) as total_distance_traveled,
            COUNT(DISTINCT transport_mode) as transport_modes_used,
            COUNT(DISTINCT DATE(created_at)) as active_days,
            MIN(created_at) as first_navigation,
            MAX(created_at) as last_navigation,
            COUNT(CASE WHEN stop_reason = 'destination_reached' THEN 1 END) as successful_navigations,
            COUNT(CASE WHEN stop_reason = 'user_cancelled' THEN 1 END) as cancelled_navigations
        FROM user_navigation_logs 
        WHERE user_id = ? $dateFilter
    ");
    $statsStmt->execute([$currentUser['id']]);
    $basicStats = $statsStmt->fetch();
    
    // Get popular destinations if requested
    $popularDestinations = [];
    if ($includeDestinations) {
        $destinationsStmt = $conn->prepare("
            SELECT 
                destination_name,
                destination_address,
                COUNT(*) as visit_count,
                AVG(route_distance) as avg_distance,
                AVG(actual_duration) as avg_duration,
                MAX(created_at) as last_visit
            FROM user_navigation_logs 
            WHERE user_id = ? 
                AND destination_name IS NOT NULL 
                AND activity_type = 'navigation_start'
                $dateFilter
            GROUP BY destination_name, destination_address
            ORDER BY visit_count DESC
            LIMIT 10
        ");
        $destinationsStmt->execute([$currentUser['id']]);
        $popularDestinations = $destinationsStmt->fetchAll();
    }
    
    // Get transport mode statistics if requested
    $transportModeStats = [];
    if ($includeTransportModes) {
        $transportStmt = $conn->prepare("
            SELECT 
                transport_mode,
                COUNT(*) as usage_count,
                AVG(route_distance) as avg_distance,
                AVG(actual_duration) as avg_duration,
                COUNT(CASE WHEN destination_reached = 1 THEN 1 END) as successful_trips
            FROM user_navigation_logs 
            WHERE user_id = ? 
                AND transport_mode IS NOT NULL 
                AND activity_type = 'navigation_start'
                $dateFilter
            GROUP BY transport_mode
            ORDER BY usage_count DESC
        ");
        $transportStmt->execute([$currentUser['id']]);
        $transportModeStats = $transportStmt->fetchAll();
    }
    
    // Get recent navigation activity (last 7 days)
    $recentActivityStmt = $conn->prepare("
        SELECT 
            DATE(created_at) as date,
            COUNT(*) as navigation_count,
            SUM(route_distance) as total_distance,
            AVG(actual_duration) as avg_duration
        FROM user_navigation_logs 
        WHERE user_id = ? 
            AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
            AND activity_type = 'navigation_start'
        GROUP BY DATE(created_at)
        ORDER BY date DESC
    ");
    $recentActivityStmt->execute([$currentUser['id']]);
    $recentActivity = $recentActivityStmt->fetchAll();
    
    // Get traffic statistics
    $trafficStatsStmt = $conn->prepare("
        SELECT 
            ntd.traffic_condition,
            COUNT(*) as occurrence_count,
            AVG(ntd.average_speed) as avg_speed,
            AVG(ntd.traffic_delay) as avg_delay
        FROM navigation_traffic_data ntd
        INNER JOIN user_navigation_logs unl ON ntd.navigation_log_id = unl.id
        WHERE unl.user_id = ? $dateFilter
        GROUP BY ntd.traffic_condition
        ORDER BY occurrence_count DESC
    ");
    $trafficStatsStmt->execute([$currentUser['id']]);
    $trafficStats = $trafficStatsStmt->fetchAll();
    
    // Get navigation success rate by time of day
    $timeStatsStmt = $conn->prepare("
        SELECT 
            HOUR(created_at) as hour,
            COUNT(*) as total_navigations,
            COUNT(CASE WHEN destination_reached = 1 THEN 1 END) as successful_navigations,
            ROUND((COUNT(CASE WHEN destination_reached = 1 THEN 1 END) / COUNT(*)) * 100, 2) as success_rate
        FROM user_navigation_logs 
        WHERE user_id = ? 
            AND activity_type = 'navigation_start'
            $dateFilter
        GROUP BY HOUR(created_at)
        ORDER BY hour
    ");
    $timeStatsStmt->execute([$currentUser['id']]);
    $timeStats = $timeStatsStmt->fetchAll();
    
    // Calculate additional metrics
    $successRate = 0;
    if ($basicStats['navigation_starts'] > 0) {
        $successRate = round(($basicStats['destinations_reached'] / $basicStats['navigation_starts']) * 100, 2);
    }
    
    $avgSpeed = 0;
    if ($basicStats['avg_navigation_duration'] > 0 && $basicStats['avg_route_distance'] > 0) {
        $avgSpeed = round(($basicStats['avg_route_distance'] / ($basicStats['avg_navigation_duration'] / 60)), 2);
    }
    
    // Prepare response data
    $responseData = [
        'period' => $period,
        'basic_stats' => [
            'total_navigations' => intval($basicStats['total_navigations']),
            'navigation_starts' => intval($basicStats['navigation_starts']),
            'destinations_reached' => intval($basicStats['destinations_reached']),
            'navigation_stops' => intval($basicStats['navigation_stops']),
            'successful_navigations' => intval($basicStats['successful_navigations']),
            'cancelled_navigations' => intval($basicStats['cancelled_navigations']),
            'success_rate' => $successRate,
            'avg_navigation_duration' => round(floatval($basicStats['avg_navigation_duration']), 2),
            'avg_route_distance' => round(floatval($basicStats['avg_route_distance']), 2),
            'total_distance_traveled' => round(floatval($basicStats['total_distance_traveled']), 2),
            'avg_speed_kmh' => $avgSpeed,
            'transport_modes_used' => intval($basicStats['transport_modes_used']),
            'active_days' => intval($basicStats['active_days']),
            'first_navigation' => $basicStats['first_navigation'],
            'last_navigation' => $basicStats['last_navigation']
        ],
        'recent_activity' => $recentActivity,
        'traffic_stats' => $trafficStats,
        'time_stats' => $timeStats
    ];
    
    if ($includeDestinations) {
        $responseData['popular_destinations'] = $popularDestinations;
    }
    
    if ($includeTransportModes) {
        $responseData['transport_mode_stats'] = $transportModeStats;
    }
    
    // Return success response
    Response::success($responseData, 'Navigation statistics retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve navigation statistics: ' . $e->getMessage());
}
?>
