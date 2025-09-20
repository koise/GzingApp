<?php
/**
 * Test script for navigation history API
 */

// Test data
$testData = [
    'user_id' => 34,
    'start_latitude' => 14.622725,
    'start_longitude' => 121.176599,
    'end_latitude' => 14.6227241,
    'end_longitude' => 121.1765994,
    'destination_name' => 'Test Destination',
    'destination_address' => 'Test Address',
    'route_distance' => 0.5,
    'estimated_duration' => 10,
    'actual_duration' => 8,
    'estimated_fare' => 15.0,
    'actual_fare' => 15.0,
    'transport_mode' => 'driving',
    'waypoints_count' => 3,
    'traffic_condition' => 'Light',
    'average_speed' => 25.0,
    'start_time' => '2025-09-14 08:00:00',
    'end_time' => '2025-09-14 08:08:00',
    'completion_time' => '2025-09-14 08:08:00'
];

// Prepare the request
$url = 'https://powderblue-pig-261057.hostingersite.com/mobile-api/navigation-history';
$data = json_encode($testData);

$options = [
    'http' => [
        'header' => "Content-Type: application/json\r\n",
        'method' => 'POST',
        'content' => $data
    ]
];

$context = stream_context_create($options);
$result = file_get_contents($url, false, $context);

if ($result === FALSE) {
    echo "Error: Failed to make request\n";
} else {
    echo "Response:\n";
    echo $result . "\n";
    
    // Try to decode JSON
    $response = json_decode($result, true);
    if ($response) {
        echo "\nDecoded response:\n";
        print_r($response);
    } else {
        echo "\nWarning: Response is not valid JSON\n";
    }
}
?>

