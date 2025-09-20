<?php
/**
 * Debug the navigation endpoints
 */

// Test database connection
echo "=== Testing Database Connection ===\n";

try {
    require_once '../config/database.php';
    $db = new Database();
    $conn = $db->getConnection();
    echo "✅ Database connection successful\n";
    
    // Test simple query
    $stmt = $conn->prepare("SELECT COUNT(*) as count FROM navigation_activity_logs WHERE user_id = ?");
    $stmt->execute([10]);
    $result = $stmt->fetch();
    echo "✅ Query successful. Logs for user 10: " . $result['count'] . "\n";
    
} catch (Exception $e) {
    echo "❌ Database error: " . $e->getMessage() . "\n";
}

echo "\n=== Testing Endpoint Files ===\n";

// Test if endpoint files exist and are readable
$endpoints = [
    '../endpoints/navigation/user-logs.php',
    '../endpoints/navigation/stats.php'
];

foreach ($endpoints as $endpoint) {
    if (file_exists($endpoint)) {
        echo "✅ $endpoint exists\n";
        if (is_readable($endpoint)) {
            echo "✅ $endpoint is readable\n";
        } else {
            echo "❌ $endpoint is not readable\n";
        }
    } else {
        echo "❌ $endpoint does not exist\n";
    }
}

echo "\n=== Testing Direct Endpoint Access ===\n";

// Test direct access to endpoints
$testUrls = [
    'https://powderblue-pig-261057.hostingersite.com/mobile-api/navigation_activity_logs/user-logs?user_id=10&limit=5',
    'https://powderblue-pig-261057.hostingersite.com/mobile-api/navigation_activity_logs/stats?user_id=10'
];

foreach ($testUrls as $url) {
    echo "Testing: $url\n";
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
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
        echo "✅ Success\n";
    } else {
        echo "❌ Error - Response: $body\n";
    }
    echo "\n";
}

echo "=== Debug Complete ===\n";
?>

