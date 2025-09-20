<?php
/**
 * Simple test for routes API
 */

header('Content-Type: application/json');

try {
    // Test database connection
    require_once '../config/database.php';
    $db = new Database();
    $conn = $db->getConnection();
    
    // Test simple query for active routes
    $stmt = $conn->prepare("SELECT id, name, description, status FROM routes WHERE status = 'active' LIMIT 5");
    $stmt->execute();
    $routes = $stmt->fetchAll();
    
    echo json_encode([
        'success' => true,
        'message' => 'Active routes retrieved successfully',
        'data' => [
            'routes' => $routes,
            'count' => count($routes)
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage(),
        'file' => $e->getFile(),
        'line' => $e->getLine()
    ]);
}
?>

