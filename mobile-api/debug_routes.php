<?php
/**
 * Debug Routes Endpoint
 * Direct access to test routes functionality
 */

// Enable error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set content type
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

echo "Starting debug...\n";

try {
    echo "1. Including database config...\n";
    require_once __DIR__ . '/config/database.php';
    
    echo "2. Creating database connection...\n";
    $db = new Database();
    $conn = $db->getConnection();
    echo "Database connected successfully!\n";
    
    echo "3. Checking if routes table exists...\n";
    $checkTable = $conn->prepare("SHOW TABLES LIKE 'routes'");
    $checkTable->execute();
    $tableExists = $checkTable->rowCount() > 0;
    echo "Routes table exists: " . ($tableExists ? 'YES' : 'NO') . "\n";
    
    if (!$tableExists) {
        echo "4. Creating routes table...\n";
        $createTable = "
            CREATE TABLE routes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                pincount INT DEFAULT 0,
                kilometer DECIMAL(10,2) DEFAULT 0.00,
                estimated_total_fare DECIMAL(10,2) DEFAULT 0.00,
                map_details JSON,
                status ENUM('active', 'inactive', 'maintenance') DEFAULT 'active',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        ";
        $conn->exec($createTable);
        echo "Routes table created!\n";
    }
    
    echo "5. Checking routes count...\n";
    $countStmt = $conn->prepare("SELECT COUNT(*) as count FROM routes");
    $countStmt->execute();
    $count = $countStmt->fetch()['count'];
    echo "Routes count: $count\n";
    
    if ($count == 0) {
        echo "6. Inserting sample routes...\n";
        $sampleRoutes = [
            [
                'name' => 'Downtown Express',
                'description' => 'Fast route through downtown area',
                'pincount' => 5,
                'kilometer' => 12.5,
                'estimated_total_fare' => 45.00,
                'map_details' => json_encode([
                    'pins' => [
                        ['lat' => 14.5995, 'lng' => 120.9842, 'name' => 'Start Point'],
                        ['lat' => 14.6042, 'lng' => 120.9822, 'name' => 'Waypoint 1'],
                        ['lat' => 14.6091, 'lng' => 120.9789, 'name' => 'Waypoint 2'],
                        ['lat' => 14.6145, 'lng' => 120.9756, 'name' => 'Waypoint 3'],
                        ['lat' => 14.6200, 'lng' => 120.9720, 'name' => 'Destination']
                    ]
                ]),
                'status' => 'active'
            ],
            [
                'name' => 'University Loop',
                'description' => 'Scenic route connecting universities',
                'pincount' => 4,
                'kilometer' => 8.3,
                'estimated_total_fare' => 32.50,
                'map_details' => json_encode([
                    'pins' => [
                        ['lat' => 14.6500, 'lng' => 121.0700, 'name' => 'Start Point'],
                        ['lat' => 14.6550, 'lng' => 121.0750, 'name' => 'Waypoint 1'],
                        ['lat' => 14.6600, 'lng' => 121.0800, 'name' => 'Waypoint 2'],
                        ['lat' => 14.6650, 'lng' => 121.0850, 'name' => 'Destination']
                    ]
                ]),
                'status' => 'active'
            ]
        ];
        
        $insertStmt = $conn->prepare("
            INSERT INTO routes (name, description, pincount, kilometer, estimated_total_fare, map_details, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        ");
        
        foreach ($sampleRoutes as $route) {
            $insertStmt->execute([
                $route['name'],
                $route['description'],
                $route['pincount'],
                $route['kilometer'],
                $route['estimated_total_fare'],
                $route['map_details'],
                $route['status']
            ]);
        }
        echo "Sample routes inserted!\n";
    }
    
    echo "7. Fetching routes...\n";
    $routesStmt = $conn->prepare("
        SELECT id, name, description, pincount, kilometer, estimated_total_fare, 
               map_details, status, created_at, updated_at
        FROM routes 
        WHERE status = 'active'
        ORDER BY created_at DESC
    ");
    $routesStmt->execute();
    $routes = $routesStmt->fetchAll();
    
    echo "8. Processing routes...\n";
    foreach ($routes as &$route) {
        if ($route['map_details']) {
            $route['map_details'] = json_decode($route['map_details'], true);
        }
    }
    
    echo "9. Returning response...\n";
    $response = [
        'success' => true,
        'message' => 'Routes retrieved successfully',
        'data' => [
            'routes' => $routes,
            'count' => count($routes)
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ];
    
    echo json_encode($response, JSON_PRETTY_PRINT);
    
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
    echo "File: " . $e->getFile() . "\n";
    echo "Line: " . $e->getLine() . "\n";
    echo "Trace: " . $e->getTraceAsString() . "\n";
    
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>

