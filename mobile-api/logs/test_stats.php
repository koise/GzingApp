<?php
/**
 * Test version of stats endpoint
 */

header('Content-Type: application/json');

try {
    // Test basic functionality first
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 10;
    
    // Test database connection
    require_once '../config/database.php';
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get basic stats
    $statsStmt = $conn->prepare("
        SELECT 
            COUNT(*) as total_logs,
            COUNT(CASE WHEN activity_type = 'navigation_start' THEN 1 END) as navigation_starts,
            COUNT(CASE WHEN activity_type = 'navigation_stop' THEN 1 END) as navigation_stops,
            COUNT(CASE WHEN destination_reached = 1 THEN 1 END) as destinations_reached,
            AVG(navigation_duration) as avg_duration,
            SUM(route_distance) as total_distance
        FROM navigation_activity_logs 
        WHERE user_id = ?
    ");
    
    $statsStmt->execute([$userId]);
    $stats = $statsStmt->fetch();
    
    // Get transport mode breakdown
    $transportStmt = $conn->prepare("
        SELECT transport_mode, COUNT(*) as count
        FROM navigation_activity_logs 
        WHERE user_id = ? AND transport_mode IS NOT NULL
        GROUP BY transport_mode
        ORDER BY count DESC
    ");
    
    $transportStmt->execute([$userId]);
    $transportBreakdown = $transportStmt->fetchAll();
    
    echo json_encode([
        'success' => true,
        'message' => 'Navigation statistics retrieved successfully',
        'data' => [
            'user_id' => $userId,
            'total_logs' => intval($stats['total_logs']),
            'navigation_starts' => intval($stats['navigation_starts']),
            'navigation_stops' => intval($stats['navigation_stops']),
            'destinations_reached' => intval($stats['destinations_reached']),
            'avg_duration' => $stats['avg_duration'] ? floatval($stats['avg_duration']) : 0,
            'total_distance' => $stats['total_distance'] ? floatval($stats['total_distance']) : 0,
            'transport_breakdown' => $transportBreakdown
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage(),
        'file' => $e->getFile(),
        'line' => $e->getLine(),
        'trace' => $e->getTraceAsString()
    ]);
}
?>

