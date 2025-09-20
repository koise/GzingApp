<?php
// Direct test endpoint for user ID 32
error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/logs/user_32_test.log');

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

// Create logs directory if it doesn't exist
$logs_dir = __DIR__ . '/logs';
if (!is_dir($logs_dir)) {
    mkdir($logs_dir, 0755, true);
}

$user_id = 32;

try {
    // Include database configuration
    include_once __DIR__ . '/config/database.php';
    
    if (!class_exists('Database')) {
        throw new Exception('Database class not found');
    }
    
    $db = new Database();
    $pdo = $db->getConnection();
    
    if (!$pdo) {
        throw new Exception('Database connection failed');
    }
    
    // Test 1: Check if table exists
    $stmt = $pdo->query("SHOW TABLES LIKE 'navigation_routes'");
    $table_exists = $stmt->rowCount() > 0;
    
    // Test 2: Get table structure
    $table_structure = null;
    if ($table_exists) {
        $stmt = $pdo->query("DESCRIBE navigation_routes");
        $table_structure = $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
    
    // Test 3: Count total routes
    $total_routes = 0;
    if ($table_exists) {
        $stmt = $pdo->query("SELECT COUNT(*) as total FROM navigation_routes");
        $total_routes = $stmt->fetch(PDO::FETCH_ASSOC)['total'];
    }
    
    // Test 4: Count routes for user 32
    $user_32_count = 0;
    if ($table_exists) {
        $stmt = $pdo->prepare("SELECT COUNT(*) as total FROM navigation_routes WHERE user_id = ?");
        $stmt->execute([$user_id]);
        $user_32_count = $stmt->fetch(PDO::FETCH_ASSOC)['total'];
    }
    
    // Test 5: Get routes for user 32
    $user_32_routes = [];
    if ($table_exists) {
        $stmt = $pdo->prepare("SELECT * FROM navigation_routes WHERE user_id = ? ORDER BY created_at DESC LIMIT 10");
        $stmt->execute([$user_id]);
        $user_32_routes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
    
    // Test 6: Get all users and their route counts
    $all_users = [];
    if ($table_exists) {
        $stmt = $pdo->query("SELECT user_id, COUNT(*) as route_count FROM navigation_routes GROUP BY user_id ORDER BY route_count DESC LIMIT 10");
        $all_users = $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
    
    // Return comprehensive test results
    echo json_encode([
        'success' => true,
        'message' => 'User 32 test results',
        'test_user_id' => $user_id,
        'tests' => [
            'table_exists' => $table_exists,
            'table_structure' => $table_structure,
            'total_routes_in_table' => $total_routes,
            'routes_for_user_32' => $user_32_count,
            'user_32_routes_data' => $user_32_routes,
            'all_users_with_routes' => $all_users
        ],
        'debug' => [
            'database_connected' => true,
            'timestamp' => date('Y-m-d H:i:s')
        ]
    ], JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    error_log("User 32 test failed: " . $e->getMessage());
    
    echo json_encode([
        'success' => false,
        'message' => 'User 32 test failed: ' . $e->getMessage(),
        'test_user_id' => $user_id,
        'error' => [
            'message' => $e->getMessage(),
            'file' => $e->getFile(),
            'line' => $e->getLine(),
            'timestamp' => date('Y-m-d H:i:s')
        ]
    ], JSON_PRETTY_PRINT);
}
?>
