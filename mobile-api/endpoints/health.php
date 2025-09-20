<?php
/**
 * Health Check Endpoint
 * GET /health
 */

// Set content type
header('Content-Type: application/json');

try {
    // Test database connection
    require_once __DIR__ . '/../config/database.php';
    $db = new Database();
    $conn = $db->getConnection();
    
    // Test basic query
    $stmt = $conn->prepare("SELECT 1 as test");
    $stmt->execute();
    $result = $stmt->fetch();
    
    // Check if routes table exists
    $checkTable = $conn->prepare("SHOW TABLES LIKE 'routes'");
    $checkTable->execute();
    $routesTableExists = $checkTable->rowCount() > 0;
    
    // Get routes count if table exists
    $routesCount = 0;
    if ($routesTableExists) {
        $countStmt = $conn->prepare("SELECT COUNT(*) as count FROM routes");
        $countStmt->execute();
        $routesCount = $countStmt->fetch()['count'];
    }
    
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'status' => 'healthy',
        'message' => 'API is running properly',
        'database' => [
            'connected' => true,
            'routes_table_exists' => $routesTableExists,
            'routes_count' => $routesCount
        ],
        'timestamp' => date('Y-m-d H:i:s'),
        'server' => [
            'php_version' => PHP_VERSION,
            'server_software' => $_SERVER['SERVER_SOFTWARE'] ?? 'Unknown'
        ]
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'status' => 'unhealthy',
        'message' => 'API health check failed: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>