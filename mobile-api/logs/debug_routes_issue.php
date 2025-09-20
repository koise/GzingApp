<?php
/**
 * Debug the routes API issue
 */

echo "=== Debugging Routes API Issue ===\n\n";

// Test 1: Check if the main routes endpoint works
echo "1. Testing main routes endpoint...\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/routes');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HEADER, true);
curl_setopt($ch, CURLOPT_TIMEOUT, 10);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
$body = substr($response, $headerSize);

curl_close($ch);

echo "HTTP Code: $httpCode\n";
if ($httpCode == 200) {
    echo "✅ Main endpoint working\n";
    $data = json_decode($body, true);
    if ($data && isset($data['success']) && $data['success']) {
        $routes = $data['data']['routes'] ?? [];
        echo "Found " . count($routes) . " routes\n";
        foreach ($routes as $route) {
            echo "  - {$route['name']} (Status: {$route['status']})\n";
        }
    } else {
        echo "❌ API returned error: " . ($data['message'] ?? 'Unknown error') . "\n";
    }
} else {
    echo "❌ HTTP Error - Response: $body\n";
}

echo "\n";

// Test 2: Check simple test endpoint
echo "2. Testing simple routes test...\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/logs/test_routes_simple.php');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HEADER, true);
curl_setopt($ch, CURLOPT_TIMEOUT, 10);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
$body = substr($response, $headerSize);

curl_close($ch);

echo "HTTP Code: $httpCode\n";
if ($httpCode == 200) {
    echo "✅ Simple test working\n";
    $data = json_decode($body, true);
    if ($data && isset($data['success']) && $data['success']) {
        $routes = $data['data']['routes'] ?? [];
        echo "Found " . count($routes) . " active routes\n";
    } else {
        echo "❌ Simple test error: " . ($data['message'] ?? 'Unknown error') . "\n";
    }
} else {
    echo "❌ Simple test HTTP Error - Response: $body\n";
}

echo "\n";

// Test 3: Check with explicit status parameter
echo "3. Testing routes with status=active...\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/routes?status=active');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HEADER, true);
curl_setopt($ch, CURLOPT_TIMEOUT, 10);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
$body = substr($response, $headerSize);

curl_close($ch);

echo "HTTP Code: $httpCode\n";
if ($httpCode == 200) {
    echo "✅ Status parameter working\n";
    $data = json_decode($body, true);
    if ($data && isset($data['success']) && $data['success']) {
        $routes = $data['data']['routes'] ?? [];
        echo "Found " . count($routes) . " active routes\n";
    } else {
        echo "❌ Status parameter error: " . ($data['message'] ?? 'Unknown error') . "\n";
    }
} else {
    echo "❌ Status parameter HTTP Error - Response: $body\n";
}

echo "\n=== Debug Complete ===\n";
?>

