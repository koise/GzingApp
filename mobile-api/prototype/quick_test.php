<?php
/**
 * Quick Test Script for getuser.php API
 * This script tests the API and shows the results
 */

echo "<h1>🧪 Quick API Test</h1>";
echo "<p>Testing the getuser.php API...</p>";

// Test different user IDs
$testUserIds = [1, 2, 3, 9, 10, 33];

foreach ($testUserIds as $userId) {
    echo "<h3>Testing User ID: $userId</h3>";
    
    $url = "https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=$userId";
    
    echo "<p><strong>URL:</strong> <a href='$url' target='_blank'>$url</a></p>";
    
    // Make the request
    $context = stream_context_create([
        'http' => [
            'method' => 'GET',
            'header' => 'User-Agent: QuickTest/1.0'
        ]
    ]);
    
    $response = @file_get_contents($url, false, $context);
    
    if ($response === false) {
        echo "<p style='color: red;'>❌ Failed to fetch data</p>";
    } else {
        $data = json_decode($response, true);
        
        if ($data && $data['success']) {
            $user = $data['data']['user'];
            echo "<p style='color: green;'>✅ Success!</p>";
            echo "<p><strong>Name:</strong> {$user['first_name']} {$user['last_name']}</p>";
            echo "<p><strong>Email:</strong> {$user['email']}</p>";
            echo "<p><strong>Status:</strong> {$user['status']}</p>";
        } else {
            echo "<p style='color: orange;'>⚠️ " . ($data['message'] ?? 'Unknown error') . "</p>";
        }
    }
    
    echo "<hr>";
}

echo "<h3>🔗 Direct Links</h3>";
echo "<ul>";
foreach ($testUserIds as $userId) {
    $url = "getuser.php?id=$userId";
    echo "<li><a href='$url' target='_blank'>User $userId</a></li>";
}
echo "</ul>";

echo "<h3>📱 Demo Pages</h3>";
echo "<ul>";
echo "<li><a href='fetch_user_demo.html' target='_blank'>Interactive Demo</a></li>";
echo "<li><a href='test_user9.html' target='_blank'>Debug User 9</a></li>";
echo "<li><a href='test_api.html' target='_blank'>API Test Page</a></li>";
echo "</ul>";
?>

