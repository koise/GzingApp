<?php
/**
 * Get Navigation Statistics Endpoint
 * GET /navigation_activity_logs/stats
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/SessionManager.php';

try {
    // Enable error reporting for debugging
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
    
    // For now, allow access without authentication for testing
    // TODO: Implement proper authentication later
    // SessionManager::requireAuth();
    // $currentUser = SessionManager::getUserData();
    
    // Debug: Log the request
    error_log("Stats endpoint called with user_id: " . ($_GET['user_id'] ?? 'not set'));
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get query parameters
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 10; // Default user ID for testing
    $dateFrom = isset($_GET['date_from']) ? $_GET['date_from'] : null;
    $dateTo = isset($_GET['date_to']) ? $_GET['date_to'] : null;
    
    // Build date filter
    $dateFilter = '';
    $params = [$userId];
    
    if ($dateFrom) {
        $dateFilter .= ' AND DATE(created_at) >= ?';
        $params[] = $dateFrom;
    }
    
    if ($dateTo) {
        $dateFilter .= ' AND DATE(created_at) <= ?';
        $params[] = $dateTo;
    }
    
    // Get total navigation sessions
    $totalSessionsStmt = $conn->prepare("
        SELECT COUNT(DISTINCT DATE(created_at)) as total_sessions 
        FROM navigation_activity_logs 
        WHERE user_id = ? $dateFilter
    ");
    $totalSessionsStmt->execute($params);
    $totalSessions = $totalSessionsStmt->fetch()['total_sessions'];
    
    // Get total distance
    $totalDistanceStmt = $conn->prepare("
        SELECT COALESCE(SUM(route_distance), 0) as total_distance 
        FROM navigation_activity_logs 
        WHERE user_id = ? AND route_distance IS NOT NULL $dateFilter
    ");
    $totalDistanceStmt->execute($params);
    $totalDistance = $totalDistanceStmt->fetch()['total_distance'];
    
    // Get total navigation time
    $totalTimeStmt = $conn->prepare("
        SELECT COALESCE(SUM(navigation_duration), 0) as total_time 
        FROM navigation_activity_logs 
        WHERE user_id = ? AND navigation_duration IS NOT NULL $dateFilter
    ");
    $totalTimeStmt->execute($params);
    $totalTime = $totalTimeStmt->fetch()['total_time'];
    
    // Get activity type breakdown
    $activityBreakdownStmt = $conn->prepare("
        SELECT activity_type, COUNT(*) as count 
        FROM navigation_activity_logs 
        WHERE user_id = ? $dateFilter
        GROUP BY activity_type
        ORDER BY count DESC
    ");
    $activityBreakdownStmt->execute($params);
    $activityBreakdown = $activityBreakdownStmt->fetchAll();
    
    // Get transport mode breakdown
    $transportBreakdownStmt = $conn->prepare("
        SELECT transport_mode, COUNT(*) as count 
        FROM navigation_activity_logs 
        WHERE user_id = ? AND transport_mode IS NOT NULL $dateFilter
        GROUP BY transport_mode
        ORDER BY count DESC
    ");
    $transportBreakdownStmt->execute($params);
    $transportBreakdown = $transportBreakdownStmt->fetchAll();
    
    // Get most visited destinations
    $destinationsStmt = $conn->prepare("
        SELECT destination_name, destination_address, COUNT(*) as visit_count,
               AVG(route_distance) as avg_distance, AVG(navigation_duration) as avg_duration
        FROM navigation_activity_logs 
        WHERE user_id = ? AND destination_name IS NOT NULL $dateFilter
        GROUP BY destination_name, destination_address
        ORDER BY visit_count DESC
        LIMIT 10
    ");
    $destinationsStmt->execute($params);
    $topDestinations = $destinationsStmt->fetchAll();
    
    // Get recent activity (last 7 days)
    $recentActivityStmt = $conn->prepare("
        SELECT DATE(created_at) as date, COUNT(*) as activity_count,
               COALESCE(SUM(route_distance), 0) as daily_distance,
               COALESCE(SUM(navigation_duration), 0) as daily_duration
        FROM navigation_activity_logs 
        WHERE user_id = ? AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) $dateFilter
        GROUP BY DATE(created_at)
        ORDER BY date DESC
    ");
    $recentActivityStmt->execute($params);
    $recentActivity = $recentActivityStmt->fetchAll();
    
    // Calculate averages
    $avgDistancePerSession = $totalSessions > 0 ? round($totalDistance / $totalSessions, 2) : 0;
    $avgTimePerSession = $totalSessions > 0 ? round($totalTime / $totalSessions, 2) : 0;
    
    Response::success([
        'user_id' => $userId,
        'period' => [
            'from' => $dateFrom,
            'to' => $dateTo
        ],
        'summary' => [
            'total_sessions' => intval($totalSessions),
            'total_distance_km' => round($totalDistance, 2),
            'total_time_minutes' => intval($totalTime),
            'avg_distance_per_session_km' => $avgDistancePerSession,
            'avg_time_per_session_minutes' => $avgTimePerSession
        ],
        'breakdown' => [
            'activity_types' => $activityBreakdown,
            'transport_modes' => $transportBreakdown
        ],
        'top_destinations' => $topDestinations,
        'recent_activity' => $recentActivity
    ], 'Navigation statistics retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve navigation statistics: ' . $e->getMessage());
}
?>
