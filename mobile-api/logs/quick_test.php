<?php
/**
 * Quick Test Script for Logging APIs
 * Test the fixes we just implemented
 */

// Test 1: Transport mode validation fix
echo "=== Test 1: Transport Mode Validation ===\n";

$testData = [
    'user_id' => 34,
    'log_type' => 'navigation',
    'activity_type' => 'navigation_start',
    'transport_mode' => 'car', // This should be converted to 'driving'
    'start_latitude' => 14.6227218,
    'start_longitude' => 121.1765988,
    'destination_name' => 'Test Destination'
];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/logs/create_log_test.php');
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($testData));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "HTTP Code: $httpCode\n";
echo "Response: $response\n\n";

// Test 2: Navigation stats endpoint
echo "=== Test 2: Navigation Stats Endpoint ===\n";

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/navigation_activity_logs/stats?user_id=34');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "HTTP Code: $httpCode\n";
echo "Response: $response\n\n";

// Test 3: User navigation logs endpoint
echo "=== Test 3: User Navigation Logs Endpoint ===\n";

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/navigation_activity_logs/user-logs?user_id=34&limit=5');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "HTTP Code: $httpCode\n";
echo "Response: $response\n\n";

// Test 4: Navigation stop endpoint
echo "=== Test 4: Navigation Stop Endpoint ===\n";

$stopData = [
    'user_id' => 34, // Using user_id instead of log_id
    'end_latitude' => 14.6227218,
    'end_longitude' => 121.1765989,
    'destination_reached' => false,
    'navigation_duration' => 180,
    'additional_data' => ['stop_reason' => 'user_cancelled']
];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/navigation_activity_logs/stop');
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($stopData));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

echo "HTTP Code: $httpCode\n";
echo "Response: $response\n\n";

echo "=== All Tests Complete ===\n";
?>

