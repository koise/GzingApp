<?php
/**
 * Test script to verify API endpoints on public domain
 */

$baseUrl = 'https://powderblue-pig-261057.hostingersite.com/mobile-api/';

echo "Testing API endpoints on: $baseUrl\n\n";

// Test 1: Health check
echo "1. Testing health check endpoint...\n";
$testUrl = $baseUrl . 'test';
$response = file_get_contents($testUrl);
echo "Response: " . $response . "\n\n";

// Test 2: API info
echo "2. Testing API info endpoint...\n";
$infoUrl = $baseUrl;
$response = file_get_contents($infoUrl);
echo "Response: " . $response . "\n\n";

// Test 3: Test with cURL for better error handling
echo "3. Testing with cURL...\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $testUrl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
curl_setopt($ch, CURLOPT_TIMEOUT, 10);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$error = curl_error($ch);
curl_close($ch);

if ($error) {
    echo "cURL Error: $error\n";
} else {
    echo "HTTP Code: $httpCode\n";
    echo "Response: $response\n";
}
?>








