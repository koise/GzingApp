<?php
/**
 * Test the routes API to verify it only returns active routes
 */

echo "=== Testing Routes API ===\n\n";

// Test 1: Get routes without status parameter (should default to active)
echo "1. Testing routes API without status parameter (should default to active)...\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/routes');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HEADER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
$body = substr($response, $headerSize);

curl_close($ch);

echo "HTTP Code: $httpCode\n";
if ($httpCode == 200) {
    $data = json_decode($body, true);
    if ($data && isset($data['success']) && $data['success']) {
        $routes = $data['data']['routes'] ?? [];
        echo "✅ Success - Found " . count($routes) . " routes\n";
        
        // Check if all routes are active
        $allActive = true;
        foreach ($routes as $route) {
            if ($route['status'] !== 'active') {
                $allActive = false;
                echo "❌ Found non-active route: {$route['name']} (status: {$route['status']})\n";
            }
        }
        
        if ($allActive) {
            echo "✅ All routes are active\n";
        }
        
        // Show route names
        echo "Active routes:\n";
        foreach ($routes as $route) {
            echo "  - {$route['name']} (ID: {$route['id']}, Status: {$route['status']})\n";
        }
    } else {
        echo "❌ API returned error: " . ($data['message'] ?? 'Unknown error') . "\n";
    }
} else {
    echo "❌ HTTP Error - Response: $body\n";
}

echo "\n";

// Test 2: Get routes with explicit status=active
echo "2. Testing routes API with status=active...\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/routes?status=active');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HEADER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
$body = substr($response, $headerSize);

curl_close($ch);

echo "HTTP Code: $httpCode\n";
if ($httpCode == 200) {
    $data = json_decode($body, true);
    if ($data && isset($data['success']) && $data['success']) {
        $routes = $data['data']['routes'] ?? [];
        echo "✅ Success - Found " . count($routes) . " active routes\n";
    } else {
        echo "❌ API returned error: " . ($data['message'] ?? 'Unknown error') . "\n";
    }
} else {
    echo "❌ HTTP Error - Response: $body\n";
}

echo "\n";

// Test 3: Get all routes (including inactive)
echo "3. Testing routes API with status=inactive...\n";
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://powderblue-pig-261057.hostingersite.com/mobile-api/routes?status=inactive');
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_HEADER, true);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
$body = substr($response, $headerSize);

curl_close($ch);

echo "HTTP Code: $httpCode\n";
if ($httpCode == 200) {
    $data = json_decode($body, true);
    if ($data && isset($data['success']) && $data['success']) {
        $routes = $data['data']['routes'] ?? [];
        echo "✅ Success - Found " . count($routes) . " inactive routes\n";
        
        // Show inactive route names
        if (count($routes) > 0) {
            echo "Inactive routes:\n";
            foreach ($routes as $route) {
                echo "  - {$route['name']} (ID: {$route['id']}, Status: {$route['status']})\n";
            }
        }
    } else {
        echo "❌ API returned error: " . ($data['message'] ?? 'Unknown error') . "\n";
    }
} else {
    echo "❌ HTTP Error - Response: $body\n";
}

echo "\n=== Test Complete ===\n";
?>

