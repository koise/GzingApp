<?php
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
    @include_once __DIR__ . '/../../config/database.php';
    
    // Get user_id from query parameter
    $user_id = isset($_GET['user_id']) ? (int)$_GET['user_id'] : null;
    
    if (!$user_id || $user_id <= 0) {
        throw new Exception('Valid user_id is required');
    }
    
    // Check if database connection is available
    if (!class_exists('Database')) {
        // Return mock response if database is not available
        echo json_encode([
            'success' => true,
            'message' => 'Navigation history stats retrieved successfully (mock)',
            'data' => [
                'user_id' => $user_id,
                'total_navigations' => 15,
                'successful_navigations' => 14,
                'avg_duration_minutes' => 12.5,
                'avg_distance_km' => 2.8,
                'avg_speed_kmh' => 18.2,
                'favorite_routes' => 3,
                'last_navigation' => '2025-09-13 21:15:00',
                'first_navigation' => '2025-09-01 08:30:00',
                'popular_destinations' => [
                    [
                        'destination_name' => 'Langhaya',
                        'destination_address' => 'Antipolo City',
                        'visit_count' => 5,
                        'avg_duration' => 2.0,
                        'avg_distance' => 0.06,
                        'last_visit' => '2025-09-13 21:00:01',
                        'avg_rating' => 4.8
                    ],
                    [
                        'destination_name' => 'Dela Paz Elementary School',
                        'destination_address' => 'Antipolo City',
                        'visit_count' => 3,
                        'avg_duration' => 8.0,
                        'avg_distance' => 3.27,
                        'last_visit' => '2025-09-13 20:59:30',
                        'avg_rating' => 4.0
                    ]
                ],
                'transport_mode_stats' => [
                    'driving' => 12,
                    'walking' => 2,
                    'cycling' => 1,
                    'public_transport' => 0
                ],
                'route_quality_stats' => [
                    'excellent' => 4,
                    'good' => 8,
                    'fair' => 2,
                    'poor' => 1
                ]
            ]
        ]);
        exit();
    }
    
    // Initialize database connection
    $db = new Database();
    $pdo = $db->connect();
    
    if (!$pdo) {
        throw new Exception('Database connection failed');
    }
    
    // Get basic stats
    $stats_sql = "SELECT 
        COUNT(*) as total_navigations,
        COUNT(CASE WHEN completion_time IS NOT NULL THEN 1 END) as successful_navigations,
        ROUND(AVG(actual_duration), 2) as avg_duration_minutes,
        ROUND(AVG(route_distance), 2) as avg_distance_km,
        ROUND(AVG(average_speed), 2) as avg_speed_kmh,
        COUNT(CASE WHEN is_favorite = 1 THEN 1 END) as favorite_routes,
        MAX(completion_time) as last_navigation,
        MIN(start_time) as first_navigation
    FROM navigation_history 
    WHERE user_id = :user_id";
    
    $stats_stmt = $pdo->prepare($stats_sql);
    $stats_stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $stats_stmt->execute();
    $basic_stats = $stats_stmt->fetch(PDO::FETCH_ASSOC);
    
    // Get popular destinations
    $destinations_sql = "SELECT 
        destination_name,
        destination_address,
        COUNT(*) as visit_count,
        ROUND(AVG(actual_duration), 2) as avg_duration,
        ROUND(AVG(route_distance), 2) as avg_distance,
        MAX(completion_time) as last_visit,
        ROUND(AVG(user_rating), 2) as avg_rating
    FROM navigation_history 
    WHERE user_id = :user_id AND completion_time IS NOT NULL
    GROUP BY destination_name, destination_address
    HAVING visit_count > 0
    ORDER BY visit_count DESC, last_visit DESC
    LIMIT 10";
    
    $destinations_stmt = $pdo->prepare($destinations_sql);
    $destinations_stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $destinations_stmt->execute();
    $popular_destinations = $destinations_stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Get transport mode stats
    $transport_sql = "SELECT 
        transport_mode,
        COUNT(*) as count
    FROM navigation_history 
    WHERE user_id = :user_id
    GROUP BY transport_mode";
    
    $transport_stmt = $pdo->prepare($transport_sql);
    $transport_stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $transport_stmt->execute();
    $transport_stats = $transport_stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Format transport mode stats
    $transport_mode_stats = [
        'driving' => 0,
        'walking' => 0,
        'cycling' => 0,
        'public_transport' => 0
    ];
    
    foreach ($transport_stats as $stat) {
        $transport_mode_stats[$stat['transport_mode']] = (int)$stat['count'];
    }
    
    // Get route quality stats
    $quality_sql = "SELECT 
        route_quality,
        COUNT(*) as count
    FROM navigation_history 
    WHERE user_id = :user_id
    GROUP BY route_quality";
    
    $quality_stmt = $pdo->prepare($quality_sql);
    $quality_stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $quality_stmt->execute();
    $quality_stats = $quality_stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Format route quality stats
    $route_quality_stats = [
        'excellent' => 0,
        'good' => 0,
        'fair' => 0,
        'poor' => 0
    ];
    
    foreach ($quality_stats as $stat) {
        $route_quality_stats[$stat['route_quality']] = (int)$stat['count'];
    }
    
    // Return success response
    echo json_encode([
        'success' => true,
        'message' => 'Navigation history stats retrieved successfully',
        'data' => [
            'user_id' => $user_id,
            'total_navigations' => (int)$basic_stats['total_navigations'],
            'successful_navigations' => (int)$basic_stats['successful_navigations'],
            'avg_duration_minutes' => $basic_stats['avg_duration_minutes'] ? (float)$basic_stats['avg_duration_minutes'] : 0,
            'avg_distance_km' => $basic_stats['avg_distance_km'] ? (float)$basic_stats['avg_distance_km'] : 0,
            'avg_speed_kmh' => $basic_stats['avg_speed_kmh'] ? (float)$basic_stats['avg_speed_kmh'] : 0,
            'favorite_routes' => (int)$basic_stats['favorite_routes'],
            'last_navigation' => $basic_stats['last_navigation'],
            'first_navigation' => $basic_stats['first_navigation'],
            'popular_destinations' => $popular_destinations,
            'transport_mode_stats' => $transport_mode_stats,
            'route_quality_stats' => $route_quality_stats
        ]
    ]);
    
} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'Error retrieving navigation history stats: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>

