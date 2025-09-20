<?php
// Enable error logging for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/../../logs/navigation_routes_errors.log');

// Create logs directory if it doesn't exist
$logs_dir = __DIR__ . '/../../logs';
if (!is_dir($logs_dir)) {
    mkdir($logs_dir, 0755, true);
}

// Log the request
$log_entry = [
    'timestamp' => date('Y-m-d H:i:s'),
    'method' => $_SERVER['REQUEST_METHOD'],
    'url' => $_SERVER['REQUEST_URI'],
    'user_agent' => $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown',
    'ip' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
    'get_params' => $_GET,
    'post_params' => $_POST
];

error_log("Navigation Routes API Request: " . json_encode($log_entry));

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Only allow GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed. Only GET requests are supported.',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    exit();
}

try {
    // Include database configuration
    error_log("Attempting to include database configuration...");
    @include_once __DIR__ . '/../../config/database.php';
    error_log("Database configuration included. Database class exists: " . (class_exists('Database') ? 'YES' : 'NO'));
    
    // Get query parameters
    $user_id = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 10;
    $limit = isset($_GET['limit']) ? min((int)$_GET['limit'], 100) : 50;
    $offset = isset($_GET['offset']) ? (int)$_GET['offset'] : 0;
    $order_by = isset($_GET['order_by']) ? $_GET['order_by'] : 'created_at';
    $order_direction = isset($_GET['order_direction']) ? strtoupper($_GET['order_direction']) : 'DESC';
    $favorites_only = isset($_GET['favorites_only']) ? (bool)$_GET['favorites_only'] : false;
    $search = isset($_GET['search']) ? $_GET['search'] : '';
    
    error_log("Query parameters - user_id: $user_id, limit: $limit, offset: $offset, order_by: $order_by, order_direction: $order_direction, favorites_only: " . ($favorites_only ? 'true' : 'false') . ", search: '$search'");
    
    // Validate order_by field
    $allowed_order_fields = ['created_at', 'updated_at', 'route_name', 'destination_name', 'usage_count', 'last_used'];
    if (!in_array($order_by, $allowed_order_fields)) {
        $order_by = 'created_at';
    }
    
    // Validate order direction
    if (!in_array($order_direction, ['ASC', 'DESC'])) {
        $order_direction = 'DESC';
    }
    
    if ($user_id <= 0) {
        $user_id = 10; // Default user ID
    }
    
    // Check if database connection is available
    if (!class_exists('Database')) {
        error_log("ERROR: Database class not found - falling back to mock response");
        // Return mock response if database is not available
        echo json_encode([
            'success' => true,
            'message' => 'Navigation routes retrieved successfully (mock - database class not found)',
            'debug' => [
                'user_id' => $user_id,
                'database_class_exists' => class_exists('Database'),
                'fallback_reason' => 'Database class not found'
            ],
            'data' => [
                'routes' => [
                    [
                        'id' => 1,
                        'user_id' => $user_id,
                        'route_name' => 'Home to Work',
                        'route_description' => 'Daily commute route to office',
                        'start_latitude' => '14.62270180',
                        'start_longitude' => '121.17656790',
                        'end_latitude' => '14.59758003',
                        'end_longitude' => '121.17244053',
                        'destination_name' => 'Office Building',
                        'destination_address' => 'Makati City',
                        'route_distance' => '3.27',
                        'estimated_duration' => 15,
                        'transport_mode' => 'driving',
                        'route_quality' => 'good',
                        'traffic_condition' => 'Moderate Traffic',
                        'average_speed' => '24.50',
                        'waypoints_count' => 5,
                        'route_coordinates' => null,
                        'estimated_fare' => '15.00',
                        'is_favorite' => 1,
                        'is_public' => 0,
                        'usage_count' => 25,
                        'last_used' => '2025-01-15 08:30:00',
                        'created_at' => '2025-01-01 10:00:00',
                        'updated_at' => '2025-01-15 08:30:00'
                    ]
                ],
                'pagination' => [
                    'total' => 1,
                    'limit' => $limit,
                    'offset' => $offset,
                    'has_more' => false
                ]
            ]
        ]);
        exit();
    }
    
    // Initialize database connection
    error_log("Attempting to create Database instance...");
    $db = new Database();
    error_log("Database instance created successfully");
    
    error_log("Attempting to get database connection...");
    $pdo = $db->getConnection();
    error_log("Database connection result: " . ($pdo ? 'SUCCESS' : 'FAILED'));
    
    if (!$pdo) {
        error_log("ERROR: Database connection failed - falling back to mock response");
        // Return mock response if database connection fails
        echo json_encode([
            'success' => true,
            'message' => 'Navigation routes retrieved successfully (mock - database connection failed)',
            'debug' => [
                'user_id' => $user_id,
                'database_class_exists' => class_exists('Database'),
                'fallback_reason' => 'Database connection failed'
            ],
            'data' => [
                'routes' => [
                    [
                        'id' => 1,
                        'user_id' => $user_id,
                        'route_name' => 'Home to Work',
                        'route_description' => 'Daily commute route to office',
                        'start_latitude' => '14.62270180',
                        'start_longitude' => '121.17656790',
                        'end_latitude' => '14.59758003',
                        'end_longitude' => '121.17244053',
                        'destination_name' => 'Office Building',
                        'destination_address' => 'Makati City',
                        'route_distance' => '3.27',
                        'estimated_duration' => 15,
                        'transport_mode' => 'driving',
                        'route_quality' => 'good',
                        'traffic_condition' => 'Moderate Traffic',
                        'average_speed' => '24.50',
                        'waypoints_count' => 5,
                        'route_coordinates' => null,
                        'estimated_fare' => '15.00',
                        'is_favorite' => 1,
                        'is_public' => 0,
                        'usage_count' => 25,
                        'last_used' => '2025-01-15 08:30:00',
                        'created_at' => '2025-01-01 10:00:00',
                        'updated_at' => '2025-01-15 08:30:00'
                    ]
                ],
                'pagination' => [
                    'total' => 1,
                    'limit' => $limit,
                    'offset' => $offset,
                    'has_more' => false
                ]
            ]
        ]);
        exit();
    }
    
    // Build WHERE clause
    $where_conditions = ['user_id = :user_id'];
    $params = [':user_id' => $user_id];
    
    if ($favorites_only) {
        $where_conditions[] = 'is_favorite = 1';
    }
    
    if (!empty($search)) {
        $where_conditions[] = '(route_name LIKE :search OR destination_name LIKE :search OR destination_address LIKE :search)';
        $params[':search'] = '%' . $search . '%';
    }
    
    $where_clause = implode(' AND ', $where_conditions);
    error_log("WHERE clause: $where_clause");
    error_log("Parameters: " . json_encode($params));
    
    // Get total count
    $count_sql = "SELECT COUNT(*) as total FROM navigation_routes WHERE $where_clause";
    error_log("Count SQL: $count_sql");
    $count_stmt = $pdo->prepare($count_sql);
    foreach ($params as $key => $value) {
        $count_stmt->bindValue($key, $value);
    }
    $count_stmt->execute();
    $total_count = $count_stmt->fetch(PDO::FETCH_ASSOC)['total'];
    error_log("Total count result: $total_count");
    
    // Get routes with pagination
    $sql = "SELECT * FROM navigation_routes 
            WHERE $where_clause 
            ORDER BY $order_by $order_direction 
            LIMIT :limit OFFSET :offset";
    
    error_log("Main SQL: $sql");
    $stmt = $pdo->prepare($sql);
    
    // Bind parameters
    foreach ($params as $key => $value) {
        $stmt->bindValue($key, $value);
    }
    $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
    $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
    
    $stmt->execute();
    $routes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    error_log("Routes found: " . count($routes));
    
    // Decode route_coordinates for each route
    foreach ($routes as &$route) {
        if ($route['route_coordinates']) {
            $route['route_coordinates'] = json_decode($route['route_coordinates'], true);
        }
    }
    
    // Calculate pagination info
    $has_more = ($offset + $limit) < $total_count;
    
    // Return success response
    http_response_code(200);
    header('Content-Type: application/json');
    echo json_encode([
        'success' => true,
        'message' => 'Navigation routes retrieved successfully',
        'debug' => [
            'user_id' => $user_id,
            'database_connected' => true,
            'total_count' => (int)$total_count,
            'routes_found' => count($routes)
        ],
        'data' => [
            'routes' => $routes,
            'pagination' => [
                'total' => (int)$total_count,
                'limit' => $limit,
                'offset' => $offset,
                'has_more' => $has_more
            ]
        ]
    ]);
    
} catch (Exception $e) {
    error_log("EXCEPTION: " . $e->getMessage());
    error_log("EXCEPTION TRACE: " . $e->getTraceAsString());
    
    http_response_code(400);
    header('Content-Type: application/json');
    echo json_encode([
        'success' => false,
        'message' => 'Error retrieving navigation routes: ' . $e->getMessage(),
        'debug' => [
            'error_message' => $e->getMessage(),
            'error_file' => $e->getFile(),
            'error_line' => $e->getLine(),
            'timestamp' => date('Y-m-d H:i:s')
        ]
    ]);
}
?>
