<?php
/**
 * Simple test to verify basic functionality
 */

header('Content-Type: application/json');

try {
    // Test database connection
    require_once '../config/database.php';
    $db = new Database();
    $conn = $db->getConnection();
    
    // Test simple query
    $stmt = $conn->prepare("SELECT COUNT(*) as count FROM navigation_activity_logs WHERE user_id = ?");
    $stmt->execute([10]);
    $result = $stmt->fetch();
    
    echo json_encode([
        'success' => true,
        'message' => 'Database connection successful',
        'data' => [
            'logs_count' => $result['count'],
            'user_id' => 10
        ]
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>

