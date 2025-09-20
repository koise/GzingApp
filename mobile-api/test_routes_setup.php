<?php
/**
 * Test Routes Setup
 * Check if routes table exists and create sample data
 */

// Enable error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set content type
header('Content-Type: application/json');

try {
    // Include database configuration
    require_once __DIR__ . '/config/database.php';
    
    $db = new Database();
    $conn = $db->getConnection();
    
    // Check if routes table exists
    $checkTable = $conn->prepare("SHOW TABLES LIKE 'routes'");
    $checkTable->execute();
    $tableExists = $checkTable->rowCount() > 0;
    
    if (!$tableExists) {
        // Create routes table
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
        echo json_encode([
            'success' => true,
            'message' => 'Routes table created successfully',
            'action' => 'table_created'
        ]);
    } else {
        echo json_encode([
            'success' => true,
            'message' => 'Routes table already exists',
            'action' => 'table_exists'
        ]);
    }
    
    // Check if there are any routes
    $countStmt = $conn->prepare("SELECT COUNT(*) as count FROM routes");
    $countStmt->execute();
    $count = $countStmt->fetch()['count'];
    
    if ($count == 0) {
        // Insert sample routes
        $sampleRoutes = [
            [
                'name' => 'Downtown Express',
                'description' => 'Fast route through downtown area with major landmarks',
                'pincount' => 5,
                'kilometer' => 12.5,
                'estimated_total_fare' => 45.00,
                'map_details' => json_encode([
                    'pins' => [
                        ['lat' => 14.5995, 'lng' => 120.9842, 'name' => 'Start Point', 'address' => 'Manila City Hall'],
                        ['lat' => 14.6042, 'lng' => 120.9822, 'name' => 'Waypoint 1', 'address' => 'Intramuros'],
                        ['lat' => 14.6091, 'lng' => 120.9789, 'name' => 'Waypoint 2', 'address' => 'Rizal Park'],
                        ['lat' => 14.6145, 'lng' => 120.9756, 'name' => 'Waypoint 3', 'address' => 'National Museum'],
                        ['lat' => 14.6200, 'lng' => 120.9720, 'name' => 'Destination', 'address' => 'Luneta Park']
                    ],
                    'route_type' => 'express',
                    'difficulty' => 'easy'
                ]),
                'status' => 'active'
            ],
            [
                'name' => 'University Loop',
                'description' => 'Scenic route connecting major universities in the area',
                'pincount' => 4,
                'kilometer' => 8.3,
                'estimated_total_fare' => 32.50,
                'map_details' => json_encode([
                    'pins' => [
                        ['lat' => 14.6500, 'lng' => 121.0700, 'name' => 'Start Point', 'address' => 'UP Diliman'],
                        ['lat' => 14.6550, 'lng' => 121.0750, 'name' => 'Waypoint 1', 'address' => 'Ateneo de Manila'],
                        ['lat' => 14.6600, 'lng' => 121.0800, 'name' => 'Waypoint 2', 'address' => 'Miriam College'],
                        ['lat' => 14.6650, 'lng' => 121.0850, 'name' => 'Destination', 'address' => 'La Salle Greenhills']
                    ],
                    'route_type' => 'scenic',
                    'difficulty' => 'moderate'
                ]),
                'status' => 'active'
            ],
            [
                'name' => 'Business District',
                'description' => 'Route through major business and commercial areas',
                'pincount' => 6,
                'kilometer' => 15.2,
                'estimated_total_fare' => 58.00,
                'map_details' => json_encode([
                    'pins' => [
                        ['lat' => 14.5500, 'lng' => 121.0000, 'name' => 'Start Point', 'address' => 'Makati CBD'],
                        ['lat' => 14.5550, 'lng' => 121.0050, 'name' => 'Waypoint 1', 'address' => 'BGC Taguig'],
                        ['lat' => 14.5600, 'lng' => 121.0100, 'name' => 'Waypoint 2', 'address' => 'Ortigas Center'],
                        ['lat' => 14.5650, 'lng' => 121.0150, 'name' => 'Waypoint 3', 'address' => 'Eastwood City'],
                        ['lat' => 14.5700, 'lng' => 121.0200, 'name' => 'Waypoint 4', 'address' => 'Quezon City Circle'],
                        ['lat' => 14.5750, 'lng' => 121.0250, 'name' => 'Destination', 'address' => 'Cubao']
                    ],
                    'route_type' => 'business',
                    'difficulty' => 'hard'
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
        
        echo json_encode([
            'success' => true,
            'message' => 'Sample routes inserted successfully',
            'action' => 'sample_data_inserted',
            'routes_count' => count($sampleRoutes)
        ]);
    } else {
        echo json_encode([
            'success' => true,
            'message' => 'Routes already exist in database',
            'action' => 'data_exists',
            'routes_count' => $count
        ]);
    }
    
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

