<?php
// Test database connection and navigation_routes table
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json');

try {
    // Include database configuration
    include_once __DIR__ . '/config/database.php';
    
    echo json_encode([
        'success' => true,
        'message' => 'Database test results',
        'tests' => [
            'database_class_exists' => class_exists('Database'),
            'database_connection' => false,
            'table_exists' => false,
            'table_structure' => null,
            'sample_data' => null
        ]
    ]);
    
    if (class_exists('Database')) {
        $db = new Database();
        $pdo = $db->getConnection();
        
        if ($pdo) {
            echo json_encode([
                'success' => true,
                'message' => 'Database test results',
                'tests' => [
                    'database_class_exists' => true,
                    'database_connection' => true,
                    'table_exists' => false,
                    'table_structure' => null,
                    'sample_data' => null
                ]
            ]);
            
            // Check if navigation_routes table exists
            $stmt = $pdo->query("SHOW TABLES LIKE 'navigation_routes'");
            $table_exists = $stmt->rowCount() > 0;
            
            if ($table_exists) {
                // Get table structure
                $stmt = $pdo->query("DESCRIBE navigation_routes");
                $table_structure = $stmt->fetchAll(PDO::FETCH_ASSOC);
                
                // Get sample data
                $stmt = $pdo->query("SELECT COUNT(*) as total FROM navigation_routes");
                $total_routes = $stmt->fetch(PDO::FETCH_ASSOC)['total'];
                
                $stmt = $pdo->query("SELECT * FROM navigation_routes LIMIT 3");
                $sample_data = $stmt->fetchAll(PDO::FETCH_ASSOC);
                
                echo json_encode([
                    'success' => true,
                    'message' => 'Database test results',
                    'tests' => [
                        'database_class_exists' => true,
                        'database_connection' => true,
                        'table_exists' => true,
                        'table_structure' => $table_structure,
                        'total_routes' => $total_routes,
                        'sample_data' => $sample_data
                    ]
                ]);
            } else {
                echo json_encode([
                    'success' => true,
                    'message' => 'Database test results',
                    'tests' => [
                        'database_class_exists' => true,
                        'database_connection' => true,
                        'table_exists' => false,
                        'table_structure' => null,
                        'sample_data' => null
                    ]
                ]);
            }
        }
    }
    
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database test failed: ' . $e->getMessage(),
        'error' => $e->getTraceAsString()
    ]);
}
?>
