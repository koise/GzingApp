<?php
/**
 * Fallback Navigation Stats Endpoint
 * Returns sample navigation stats data without database connection
 */

// Set content type and CORS headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

try {
    // Get query parameters
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;
    $period = isset($_GET['period']) ? trim($_GET['period']) : 'all';
    
    // Sample navigation stats data
    $sampleStats = [
        'basicStats' => [
            'totalNavigations' => 15,
            'successfulNavigations' => 12,
            'failedNavigations' => 3,
            'totalDistance' => 125.5,
            'totalDuration' => 285,
            'averageDistance' => 8.37,
            'averageDuration' => 19.0,
            'successRate' => 80.0
        ],
        'popularDestinations' => [
            [
                'destination' => 'Luneta Park',
                'count' => 5,
                'percentage' => 33.3
            ],
            [
                'destination' => 'La Salle Greenhills',
                'count' => 3,
                'percentage' => 20.0
            ],
            [
                'destination' => 'Cubao',
                'count' => 2,
                'percentage' => 13.3
            ],
            [
                'destination' => 'Makati CBD',
                'count' => 2,
                'percentage' => 13.3
            ],
            [
                'destination' => 'UP Diliman',
                'count' => 1,
                'percentage' => 6.7
            ]
        ],
        'transportModeStats' => [
            [
                'mode' => 'driving',
                'count' => 12,
                'percentage' => 80.0,
                'averageDistance' => 9.2,
                'averageDuration' => 20.5
            ],
            [
                'mode' => 'walking',
                'count' => 2,
                'percentage' => 13.3,
                'averageDistance' => 2.1,
                'averageDuration' => 15.0
            ],
            [
                'mode' => 'cycling',
                'count' => 1,
                'percentage' => 6.7,
                'averageDistance' => 5.5,
                'averageDuration' => 18.0
            ]
        ],
        'timeStats' => [
            'mostActiveHour' => 14,
            'mostActiveDay' => 'Friday',
            'peakHours' => [8, 14, 18],
            'weekendUsage' => 25.0
        ]
    ];
    
    // Return success response
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Navigation stats retrieved successfully (fallback data)',
        'data' => $sampleStats,
        'timestamp' => date('Y-m-d H:i:s'),
        'note' => 'This is fallback data. Database connection is not available.'
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Failed to retrieve navigation stats: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>

