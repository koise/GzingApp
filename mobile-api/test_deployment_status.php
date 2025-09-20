<?php
/**
 * Deployment Status Test
 * Check which endpoints are available on the production server
 */

// Enable error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set content type
header('Content-Type: application/json');

$baseUrl = 'https://powderblue-pig-261057.hostingersite.com/mobile-api';

echo "🔍 Checking Production API Deployment Status\n";
echo "==========================================\n\n";

$endpoints = [
    'health' => '/health',
    'info' => '/',
    'routes' => '/routes',
    'landmarks' => '/landmarks',
    'users' => '/users',
    'sos' => '/sos'
];

$results = [];

foreach ($endpoints as $name => $path) {
    $url = $baseUrl . $path;
    echo "Testing: $name ($url)\n";
    
    $result = makeRequest($url);
    $status = $result['http_code'];
    $success = $status >= 200 && $status < 300;
    
    echo "  Status: " . ($success ? "✅ $status" : "❌ $status") . "\n";
    
    if ($result['error']) {
        echo "  Error: " . $result['error'] . "\n";
    }
    
    $results[$name] = [
        'url' => $url,
        'status_code' => $status,
        'success' => $success,
        'error' => $result['error']
    ];
    
    echo "\n";
}

echo "📊 Deployment Status Summary\n";
echo "===========================\n";

foreach ($results as $name => $result) {
    $status = $result['success'] ? "✅ Available" : "❌ Not Available";
    echo "$name: $status (HTTP $result[status_code])\n";
}

echo "\n🎯 Next Steps:\n";
echo "==============\n";

if (!$results['landmarks']['success']) {
    echo "❌ Landmarks endpoint is not available\n";
    echo "📁 Files to upload to production server:\n";
    echo "   - mobile-api/endpoints/landmarks/\n";
    echo "   - mobile-api/endpoints/landmarks/get_landmarks.php\n";
    echo "   - mobile-api/endpoints/landmarks/get_landmarks_fallback.php\n";
    echo "   - mobile-api/endpoints/landmarks/index.php\n";
    echo "   - Update mobile-api/index.php to include landmarks route\n";
    echo "\n📋 Upload Instructions:\n";
    echo "1. Upload the landmarks directory to: /mobile-api/endpoints/landmarks/\n";
    echo "2. Update the main index.php file to include the landmarks route\n";
    echo "3. Test the endpoint: https://powderblue-pig-261057.hostingersite.com/mobile-api/landmarks\n";
} else {
    echo "✅ Landmarks endpoint is available and working!\n";
}

echo "\n🔗 Test Commands:\n";
echo "================\n";
echo "# Test health endpoint\n";
echo "curl \"$baseUrl/health\"\n\n";
echo "# Test routes endpoint\n";
echo "curl \"$baseUrl/routes\"\n\n";
echo "# Test landmarks endpoint (after deployment)\n";
echo "curl \"$baseUrl/landmarks\"\n\n";
echo "# Test landmarks with pagination\n";
echo "curl \"$baseUrl/landmarks?page=1&limit=5\"\n\n";
echo "# Test landmarks search\n";
echo "curl \"$baseUrl/landmarks?search=LRT\"\n\n";

/**
 * Helper function to make HTTP requests
 */
function makeRequest($url) {
    $ch = curl_init();
    
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Accept: application/json',
        'User-Agent: Deployment-Test/1.0'
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
