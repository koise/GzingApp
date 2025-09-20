<?php
/**
 * Test the fixed endpoints
 */

echo "=== Testing Navigation Endpoints ===\n\n";

// Test 1: User Navigation Logs
echo "1. Testing user-logs endpoint...\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/navigation_activity_logs/user-logs?user_id=10&limit=5');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HEADER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
$body = substr($response, $headerSize);

curl_close($ch);

echo "HTTP Code: $httpCode\n";
echo "Response Body: $body\n\n";

// Test 2: Navigation Stats
echo "2. Testing stats endpoint...\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/navigation_activity_logs/stats?user_id=10');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HEADER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
$body = substr($response, $headerSize);

curl_close($ch);

echo "HTTP Code: $httpCode\n";
echo "Response Body: $body\n\n";

echo "=== Test Complete ===\n";
?>

