<?php
/**
 * Landmarks API Test Suite
 * Tests the landmarks mobile API endpoint
 */

// Enable error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set content type
header('Content-Type: application/json');

// Test configuration
$baseUrl = 'https://powderblue-pig-261057.hostingersite.com/mobile-api';
$landmarksEndpoint = $baseUrl . '/landmarks';

// Test results storage
$testResults = [];

/**
 * Helper function to make HTTP requests
 */
function makeRequest($url, $method = 'GET', $data = null) {
    $ch = curl_init();
    
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Accept: application/json'
    ]);
    
    if ($method === 'POST' && $data) {
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    }
    
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

/**
 * Test function
 */
function runTest($testName, $url, $expectedCode = 200, $description = '') {
    global $testResults;
    
    echo "🧪 Running test: $testName\n";
    if ($description) {
        echo "   Description: $description\n";
    }
    echo "   URL: $url\n";
    
    $result = makeRequest($url);
    
    $testResult = [
        'test_name' => $testName,
        'url' => $url,
        'expected_code' => $expectedCode,
        'actual_code' => $result['http_code'],
        'success' => $result['http_code'] === $expectedCode,
        'response' => $result['response'],
        'error' => $result['error'],
        'description' => $description
    ];
    
    if ($testResult['success']) {
        echo "   ✅ PASSED\n";
    } else {
        echo "   ❌ FAILED (Expected: $expectedCode, Got: {$result['http_code']})\n";
        if ($result['error']) {
            echo "   Error: {$result['error']}\n";
        }
    }
    
    $testResults[] = $testResult;
    echo "\n";
    
    return $testResult;
}

/**
 * Parse and display response data
 */
function displayResponse($response) {
    $data = json_decode($response, true);
    if ($data) {
        echo "   Response Data:\n";
        echo "   - Success: " . ($data['success'] ? 'true' : 'false') . "\n";
        echo "   - Message: " . ($data['message'] ?? 'N/A') . "\n";
        
        if (isset($data['data']['landmarks'])) {
            echo "   - Landmarks Count: " . count($data['data']['landmarks']) . "\n";
        }
        
        if (isset($data['data']['pagination'])) {
            $pagination = $data['data']['pagination'];
            echo "   - Pagination: Page {$pagination['current_page']} of {$pagination['total_pages']} (Total: {$pagination['total_items']})\n";
        }
        
        if (isset($data['data']['filters']['categories'])) {
            echo "   - Available Categories: " . implode(', ', $data['data']['filters']['categories']) . "\n";
        }
    }
}

echo "🚀 Starting Landmarks API Test Suite\n";
echo "=====================================\n\n";

// Test 1: Basic landmarks endpoint
$test1 = runTest(
    'Basic Landmarks Endpoint',
    $landmarksEndpoint,
    200,
    'Test basic GET request to landmarks endpoint'
);
if ($test1['success']) {
    displayResponse($test1['response']);
}

// Test 2: Pagination test
$test2 = runTest(
    'Pagination Test',
    $landmarksEndpoint . '?page=1&limit=5',
    200,
    'Test pagination with page=1 and limit=5'
);
if ($test2['success']) {
    displayResponse($test2['response']);
}

// Test 3: Search functionality
$test3 = runTest(
    'Search Functionality',
    $landmarksEndpoint . '?search=LRT',
    200,
    'Test search functionality with "LRT" keyword'
);
if ($test3['success']) {
    displayResponse($test3['response']);
}

// Test 4: Category filtering
$test4 = runTest(
    'Category Filtering',
    $landmarksEndpoint . '?category=transport',
    200,
    'Test category filtering for transport landmarks'
);
if ($test4['success']) {
    displayResponse($test4['response']);
}

// Test 5: Location-based filtering
$test5 = runTest(
    'Location-based Filtering',
    $landmarksEndpoint . '?lat=14.6255&lng=121.1756&radius=10',
    200,
    'Test location-based filtering with coordinates and radius'
);
if ($test5['success']) {
    displayResponse($test5['response']);
}

// Test 6: Combined filters
$test6 = runTest(
    'Combined Filters',
    $landmarksEndpoint . '?search=Antipolo&category=business&page=1&limit=3',
    200,
    'Test combined search, category, and pagination filters'
);
if ($test6['success']) {
    displayResponse($test6['response']);
}

// Test 7: Invalid parameters
$test7 = runTest(
    'Invalid Parameters',
    $landmarksEndpoint . '?page=0&limit=1000',
    200,
    'Test with invalid parameters (should be handled gracefully)'
);
if ($test7['success']) {
    displayResponse($test7['response']);
}

// Test 8: Large limit test
$test8 = runTest(
    'Large Limit Test',
    $landmarksEndpoint . '?limit=100',
    200,
    'Test with large limit (should be capped at 100)'
);
if ($test8['success']) {
    displayResponse($test8['response']);
}

// Test 9: Non-existent category
$test9 = runTest(
    'Non-existent Category',
    $landmarksEndpoint . '?category=nonexistent',
    200,
    'Test with non-existent category (should return empty results)'
);
if ($test9['success']) {
    displayResponse($test9['response']);
}

// Test 10: POST method (should fail)
$test10 = runTest(
    'POST Method Test',
    $landmarksEndpoint,
    405,
    'Test POST method (should return 405 Method Not Allowed)'
);

echo "📊 Test Summary\n";
echo "===============\n";

$totalTests = count($testResults);
$passedTests = count(array_filter($testResults, function($test) {
    return $test['success'];
}));
$failedTests = $totalTests - $passedTests;

echo "Total Tests: $totalTests\n";
echo "Passed: $passedTests\n";
echo "Failed: $failedTests\n";
echo "Success Rate: " . round(($passedTests / $totalTests) * 100, 2) . "%\n\n";

if ($failedTests > 0) {
    echo "❌ Failed Tests:\n";
    foreach ($testResults as $test) {
        if (!$test['success']) {
            echo "- {$test['test_name']}: Expected {$test['expected_code']}, got {$test['actual_code']}\n";
        }
    }
    echo "\n";
}

echo "✅ Passed Tests:\n";
foreach ($testResults as $test) {
    if ($test['success']) {
        echo "- {$test['test_name']}\n";
    }
}

echo "\n🎯 API Endpoint Information:\n";
echo "Base URL: $baseUrl\n";
echo "Landmarks Endpoint: $landmarksEndpoint\n";
echo "Available Parameters:\n";
echo "- page: Page number (default: 1)\n";
echo "- limit: Items per page (default: 50, max: 100)\n";
echo "- search: Search in name, description, address\n";
echo "- category: Filter by category\n";
echo "- lat: Latitude for location filtering\n";
echo "- lng: Longitude for location filtering\n";
echo "- radius: Radius in kilometers for location filtering\n";

echo "\n📱 Mobile API Features:\n";
echo "- Only active landmarks are returned\n";
echo "- Optimized for mobile consumption\n";
echo "- Pagination support\n";
echo "- Search functionality\n";
echo "- Category filtering\n";
echo "- Location-based filtering\n";
echo "- Fallback data when database is unavailable\n";

echo "\n🔗 Example Usage:\n";
echo "GET $landmarksEndpoint\n";
echo "GET $landmarksEndpoint?search=cafe&category=business\n";
echo "GET $landmarksEndpoint?lat=14.6255&lng=121.1756&radius=5\n";
echo "GET $landmarksEndpoint?page=2&limit=10\n";

echo "\n✨ Test completed!\n";
?>
