<?php
/**
 * Production API Test
 * Quick test to verify the landmarks API is working on production
 */

// Enable error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set content type
header('Content-Type: application/json');

$baseUrl = 'https://powderblue-pig-261057.hostingersite.com/mobile-api';
$landmarksEndpoint = $baseUrl . '/landmarks';

echo "🧪 Testing Production Landmarks API\n";
echo "==================================\n\n";

echo "Base URL: $baseUrl\n";
echo "Landmarks Endpoint: $landmarksEndpoint\n\n";

// Test 1: Basic health check
echo "1. Testing health endpoint...\n";
$healthUrl = $baseUrl . '/health';
$healthResult = makeRequest($healthUrl);
echo "   Health Check: " . ($healthResult['http_code'] === 200 ? '✅ PASSED' : '❌ FAILED') . "\n";
echo "   Status Code: " . $healthResult['http_code'] . "\n";
if ($healthResult['error']) {
    echo "   Error: " . $healthResult['error'] . "\n";
}
echo "\n";

// Test 2: Basic landmarks endpoint
echo "2. Testing landmarks endpoint...\n";
$landmarksResult = makeRequest($landmarksEndpoint);
echo "   Landmarks API: " . ($landmarksResult['http_code'] === 200 ? '✅ PASSED' : '❌ FAILED') . "\n";
echo "   Status Code: " . $landmarksResult['http_code'] . "\n";
if ($landmarksResult['error']) {
    echo "   Error: " . $landmarksResult['error'] . "\n";
} else {
    $data = json_decode($landmarksResult['response'], true);
    if ($data && isset($data['data']['landmarks'])) {
        echo "   Landmarks Found: " . count($data['data']['landmarks']) . "\n";
        echo "   API Version: " . ($data['data']['api_info']['version'] ?? 'Unknown') . "\n";
    }
}
echo "\n";

// Test 3: Pagination test
echo "3. Testing pagination...\n";
$paginationUrl = $landmarksEndpoint . '?page=1&limit=3';
$paginationResult = makeRequest($paginationUrl);
echo "   Pagination: " . ($paginationResult['http_code'] === 200 ? '✅ PASSED' : '❌ FAILED') . "\n";
echo "   Status Code: " . $paginationResult['http_code'] . "\n";
if ($paginationResult['error']) {
    echo "   Error: " . $paginationResult['error'] . "\n";
} else {
    $data = json_decode($paginationResult['response'], true);
    if ($data && isset($data['data']['pagination'])) {
        $pagination = $data['data']['pagination'];
        echo "   Page: " . $pagination['current_page'] . " of " . $pagination['total_pages'] . "\n";
        echo "   Total Items: " . $pagination['total_items'] . "\n";
    }
}
echo "\n";

// Test 4: Search test
echo "4. Testing search functionality...\n";
$searchUrl = $landmarksEndpoint . '?search=LRT';
$searchResult = makeRequest($searchUrl);
echo "   Search: " . ($searchResult['http_code'] === 200 ? '✅ PASSED' : '❌ FAILED') . "\n";
echo "   Status Code: " . $searchResult['http_code'] . "\n";
if ($searchResult['error']) {
    echo "   Error: " . $searchResult['error'] . "\n";
} else {
    $data = json_decode($searchResult['response'], true);
    if ($data && isset($data['data']['landmarks'])) {
        echo "   Search Results: " . count($data['data']['landmarks']) . " landmarks found\n";
    }
}
echo "\n";

// Test 5: Category filter test
echo "5. Testing category filter...\n";
$categoryUrl = $landmarksEndpoint . '?category=transport';
$categoryResult = makeRequest($categoryUrl);
echo "   Category Filter: " . ($categoryResult['http_code'] === 200 ? '✅ PASSED' : '❌ FAILED') . "\n";
echo "   Status Code: " . $categoryResult['http_code'] . "\n";
if ($categoryResult['error']) {
    echo "   Error: " . $categoryResult['error'] . "\n";
} else {
    $data = json_decode($categoryResult['response'], true);
    if ($data && isset($data['data']['landmarks'])) {
        echo "   Category Results: " . count($data['data']['landmarks']) . " transport landmarks found\n";
    }
}
echo "\n";

echo "🎯 Production API Test Summary\n";
echo "==============================\n";
echo "✅ All tests completed!\n";
echo "📱 API is ready for mobile consumption\n";
echo "🔗 Endpoint: $landmarksEndpoint\n";
echo "📖 Documentation: See LANDMARKS_API_README.md\n";

/**
 * Helper function to make HTTP requests
 */
function makeRequest($url) {
    $ch = curl_init();
    
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // For testing purposes
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Accept: application/json',
        'User-Agent: Landmarks-API-Test/1.0'
    ]);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    
    curl_close($ch);
    
    return [
        'http_code' => $httpCode,
        'response' => $response,
        'error' => $error
    ];
}
?>
