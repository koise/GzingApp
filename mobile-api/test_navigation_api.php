<?php
/**
 * Test Navigation API Endpoints
 * Demonstrates how to use the new navigation API with the user_navigation_logs table
 */

// Test configuration
$baseUrl = 'http://localhost/mobile-api/endpoints/navigation';
$testUserId = 1; // Replace with actual user ID for testing

echo "<h1>Navigation API Test</h1>\n";
echo "<style>
    body { font-family: Arial, sans-serif; margin: 20px; }
    .test-section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
    .success { background-color: #d4edda; border-color: #c3e6cb; }
    .error { background-color: #f8d7da; border-color: #f5c6cb; }
    .request { background-color: #e2e3e5; border-color: #d6d8db; }
    pre { background-color: #f8f9fa; padding: 10px; border-radius: 3px; overflow-x: auto; }
</style>\n";

/**
 * Make HTTP request
 */
function makeRequest($url, $method = 'GET', $data = null, $headers = []) {
    $ch = curl_init();
    
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array_merge([
        'Content-Type: application/json',
        'Accept: application/json'
    ], $headers));
    
    if ($data && in_array($method, ['POST', 'PUT'])) {
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    }
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    return [
        'code' => $httpCode,
        'body' => $response,
        'data' => json_decode($response, true)
    ];
}

/**
 * Test 1: Create Navigation Start Log
 */
echo "<div class='test-section'>\n";
echo "<h2>Test 1: Create Navigation Start Log</h2>\n";

$navigationStartData = [
    'activity_type' => 'navigation_start',
    'start_latitude' => 14.5995,
    'start_longitude' => 120.9842,
    'destination_name' => 'Mall of Asia',
    'destination_address' => 'Seaside Blvd, Pasay, Metro Manila',
    'route_distance' => 15.5,
    'estimated_duration' => 25,
    'transport_mode' => 'driving',
    'device_model' => 'Samsung Galaxy S21',
    'device_id' => 'test_device_123',
    'battery_level' => 85,
    'network_type' => 'WiFi',
    'gps_accuracy' => 'high',
    'screen_resolution' => '1080x2400',
    'available_storage' => 50000000000,
    'app_version' => '1.0.0',
    'os_version' => 'Android 12',
    'additional_data' => [
        'traffic_enabled' => true,
        'route_alternatives' => 3,
        'weather' => 'sunny'
    ],
    'route_instructions' => [
        [
            'instruction' => 'Head north on Roxas Blvd',
            'distance' => 500,
            'duration' => 60,
            'maneuver' => 'straight'
        ],
        [
            'instruction' => 'Turn right onto Seaside Blvd',
            'distance' => 200,
            'duration' => 30,
            'maneuver' => 'turn-right'
        ]
    ],
    'waypoints' => [
        [
            'lat' => 14.5995,
            'lng' => 120.9842,
            'name' => 'Starting Point',
            'type' => 'start',
            'address' => 'Manila, Philippines'
        ],
        [
            'lat' => 14.5356,
            'lng' => 120.9821,
            'name' => 'Mall of Asia',
            'type' => 'destination',
            'address' => 'Seaside Blvd, Pasay, Metro Manila'
        ]
    ],
    'route_polylines' => [
        [
            'type' => 'api_response',
            'data' => 'encoded_polyline_data_here',
            'color' => '#D2B48C',
            'width' => 6.0,
            'opacity' => 0.8
        ],
        [
            'type' => 'mapbox_directions',
            'data' => 'encoded_mapbox_polyline_data_here',
            'color' => '#1976D2',
            'width' => 6.0,
            'opacity' => 0.9
        ]
    ],
    'traffic_data' => [
        'condition' => 'Moderate Traffic',
        'average_speed' => 25.5,
        'delay' => 5,
        'duration_with_traffic' => 30,
        'duration_without_traffic' => 25,
        'enabled' => true
    ],
    'navigation_events' => [
        [
            'type' => 'navigation_start',
            'data' => ['started_at' => date('Y-m-d H:i:s')],
            'latitude' => 14.5995,
            'longitude' => 120.9842
        ]
    ]
];

echo "<div class='request'>\n";
echo "<h3>Request:</h3>\n";
echo "<pre>" . json_encode($navigationStartData, JSON_PRETTY_PRINT) . "</pre>\n";
echo "</div>\n";

$response = makeRequest($baseUrl, 'POST', $navigationStartData);
$logId = null;

if ($response['code'] === 200 && $response['data']['success']) {
    echo "<div class='success'>\n";
    echo "<h3>Response (Success):</h3>\n";
    echo "<pre>" . json_encode($response['data'], JSON_PRETTY_PRINT) . "</pre>\n";
    $logId = $response['data']['data']['log_id'];
    echo "</div>\n";
} else {
    echo "<div class='error'>\n";
    echo "<h3>Response (Error):</h3>\n";
    echo "<pre>" . json_encode($response, JSON_PRETTY_PRINT) . "</pre>\n";
    echo "</div>\n";
}

echo "</div>\n";

/**
 * Test 2: Get Navigation Logs
 */
echo "<div class='test-section'>\n";
echo "<h2>Test 2: Get Navigation Logs</h2>\n";

$response = makeRequest($baseUrl . '?page=1&limit=10&include_details=true');

if ($response['code'] === 200 && $response['data']['success']) {
    echo "<div class='success'>\n";
    echo "<h3>Response (Success):</h3>\n";
    echo "<pre>" . json_encode($response['data'], JSON_PRETTY_PRINT) . "</pre>\n";
    echo "</div>\n";
} else {
    echo "<div class='error'>\n";
    echo "<h3>Response (Error):</h3>\n";
    echo "<pre>" . json_encode($response, JSON_PRETTY_PRINT) . "</pre>\n";
    echo "</div>\n";
}

echo "</div>\n";

/**
 * Test 3: Get Navigation Statistics
 */
echo "<div class='test-section'>\n";
echo "<h2>Test 3: Get Navigation Statistics</h2>\n";

$response = makeRequest($baseUrl . '/stats?period=all&include_destinations=true&include_transport_modes=true');

if ($response['code'] === 200 && $response['data']['success']) {
    echo "<div class='success'>\n";
    echo "<h3>Response (Success):</h3>\n";
    echo "<pre>" . json_encode($response['data'], JSON_PRETTY_PRINT) . "</pre>\n";
    echo "</div>\n";
} else {
    echo "<div class='error'>\n";
    echo "<h3>Response (Error):</h3>\n";
    echo "<pre>" . json_encode($response, JSON_PRETTY_PRINT) . "</pre>\n";
    echo "</div>\n";
}

echo "</div>\n";

/**
 * Test 4: Update Navigation Log (if we have a log ID)
 */
if ($logId) {
    echo "<div class='test-section'>\n";
    echo "<h2>Test 4: Update Navigation Log</h2>\n";
    
    $updateData = [
        'activity_type' => 'navigation_stop',
        'end_latitude' => 14.5356,
        'end_longitude' => 120.9821,
        'destination_reached' => true,
        'actual_duration' => 28,
        'stop_reason' => 'destination_reached',
        'additional_data' => [
            'actual_distance' => 15.2,
            'fuel_consumed' => 1.2,
            'weather_conditions' => 'sunny'
        ],
        'navigation_events' => [
            [
                'type' => 'destination_reached',
                'data' => ['arrived_at' => date('Y-m-d H:i:s')],
                'latitude' => 14.5356,
                'longitude' => 120.9821
            ]
        ]
    ];
    
    echo "<div class='request'>\n";
    echo "<h3>Request:</h3>\n";
    echo "<pre>" . json_encode($updateData, JSON_PRETTY_PRINT) . "</pre>\n";
    echo "</div>\n";
    
    $response = makeRequest($baseUrl . '/' . $logId, 'PUT', $updateData);
    
    if ($response['code'] === 200 && $response['data']['success']) {
        echo "<div class='success'>\n";
        echo "<h3>Response (Success):</h3>\n";
        echo "<pre>" . json_encode($response['data'], JSON_PRETTY_PRINT) . "</pre>\n";
        echo "</div>\n";
    } else {
        echo "<div class='error'>\n";
        echo "<h3>Response (Error):</h3>\n";
        echo "<pre>" . json_encode($response, JSON_PRETTY_PRINT) . "</pre>\n";
        echo "</div>\n";
    }
    
    echo "</div>\n";
    
    /**
     * Test 5: Get Navigation Log Detail
     */
    echo "<div class='test-section'>\n";
    echo "<h2>Test 5: Get Navigation Log Detail</h2>\n";
    
    $response = makeRequest($baseUrl . '/' . $logId);
    
    if ($response['code'] === 200 && $response['data']['success']) {
        echo "<div class='success'>\n";
        echo "<h3>Response (Success):</h3>\n";
        echo "<pre>" . json_encode($response['data'], JSON_PRETTY_PRINT) . "</pre>\n";
        echo "</div>\n";
    } else {
        echo "<div class='error'>\n";
        echo "<h3>Response (Error):</h3>\n";
        echo "<pre>" . json_encode($response, JSON_PRETTY_PRINT) . "</pre>\n";
        echo "</div>\n";
    }
    
    echo "</div>\n";
}

/**
 * Test 6: Stop Navigation (Alternative method)
 */
if ($logId) {
    echo "<div class='test-section'>\n";
    echo "<h2>Test 6: Stop Navigation</h2>\n";
    
    $stopData = [
        'log_id' => $logId,
        'end_latitude' => 14.5356,
        'end_longitude' => 120.9821,
        'destination_reached' => true,
        'navigation_duration' => 28,
        'actual_distance' => 15.2,
        'stop_reason' => 'destination_reached',
        'additional_data' => [
            'completion_time' => date('Y-m-d H:i:s'),
            'user_rating' => 5
        ]
    ];
    
    echo "<div class='request'>\n";
    echo "<h3>Request:</h3>\n";
    echo "<pre>" . json_encode($stopData, JSON_PRETTY_PRINT) . "</pre>\n";
    echo "</div>\n";
    
    $response = makeRequest($baseUrl . '/stop', 'POST', $stopData);
    
    if ($response['code'] === 200 && $response['data']['success']) {
        echo "<div class='success'>\n";
        echo "<h3>Response (Success):</h3>\n";
        echo "<pre>" . json_encode($response['data'], JSON_PRETTY_PRINT) . "</pre>\n";
        echo "</div>\n";
    } else {
        echo "<div class='error'>\n";
        echo "<h3>Response (Error):</h3>\n";
        echo "<pre>" . json_encode($response, JSON_PRETTY_PRINT) . "</pre>\n";
        echo "</div>\n";
    }
    
    echo "</div>\n";
}

echo "<h2>API Endpoints Summary</h2>\n";
echo "<div class='test-section'>\n";
echo "<h3>Available Endpoints:</h3>\n";
echo "<ul>\n";
echo "<li><strong>POST /navigation</strong> - Create new navigation log</li>\n";
echo "<li><strong>GET /navigation</strong> - Get navigation logs with filtering and pagination</li>\n";
echo "<li><strong>GET /navigation/stats</strong> - Get navigation statistics</li>\n";
echo "<li><strong>GET /navigation/{log_id}</strong> - Get detailed navigation log</li>\n";
echo "<li><strong>PUT /navigation/{log_id}</strong> - Update navigation log</li>\n";
echo "<li><strong>POST /navigation/stop</strong> - Stop navigation session</li>\n";
echo "</ul>\n";
echo "</div>\n";

echo "<h2>Database Tables Used</h2>\n";
echo "<div class='test-section'>\n";
echo "<ul>\n";
echo "<li><strong>user_navigation_logs</strong> - Main navigation log table</li>\n";
echo "<li><strong>navigation_route_instructions</strong> - Turn-by-turn directions</li>\n";
echo "<li><strong>navigation_waypoints</strong> - Route waypoints</li>\n";
echo "<li><strong>navigation_route_polylines</strong> - Route geometry data</li>\n";
echo "<li><strong>navigation_traffic_data</strong> - Traffic information</li>\n";
echo "<li><strong>navigation_events</strong> - Navigation events and milestones</li>\n";
echo "</ul>\n";
echo "</div>\n";

echo "<h2>Features</h2>\n";
echo "<div class='test-section'>\n";
echo "<ul>\n";
echo "<li>✅ Complete navigation tracking</li>\n";
echo "<li>✅ Route instructions and waypoints</li>\n";
echo "<li>✅ Traffic data integration</li>\n";
echo "<li>✅ Navigation events logging</li>\n";
echo "<li>✅ Statistics and analytics</li>\n";
echo "<li>✅ Pagination and filtering</li>\n";
echo "<li>✅ JSON data support</li>\n";
echo "<li>✅ Transaction safety</li>\n";
echo "<li>✅ Input validation</li>\n";
echo "<li>✅ Error handling</li>\n";
echo "</ul>\n";
echo "</div>\n";
?>

