<?php
/**
 * Simple SMS API Test
 * Quick test to verify the emergency SMS API is working
 */

echo "🧪 Simple Emergency SMS API Test\n";
echo "================================\n\n";

// Test data
$test_data = [
    'user_id' => 10,
    'latitude' => 14.5995,
    'longitude' => 120.9842,
    'emergency_type' => 'emergency',
    'message' => 'Test emergency message from GzingApp',
    'contacts' => ['09123456789'] // Use a test number
];

echo "📋 Test Parameters:\n";
echo "Location: Manila, Philippines (14.5995, 120.9842)\n";
echo "Contact: 09123456789\n";
echo "Message: Test emergency message\n\n";

// Make the API call
$url = 'http://localhost/mobile-api/endpoints/sms/send_emergency_sms.php';
$data = json_encode($test_data);

$options = [
    'http' => [
        'header' => "Content-type: application/json\r\n",
        'method' => 'POST',
        'content' => $data
    ]
];

$context = stream_context_create($options);

echo "📤 Sending test SMS...\n";

try {
    $response = file_get_contents($url, false, $context);
    
    if ($response === false) {
        echo "❌ Failed to get response from API\n";
        exit(1);
    }
    
    $result = json_decode($response, true);
    
    echo "📥 Response received:\n";
    echo json_encode($result, JSON_PRETTY_PRINT) . "\n\n";
    
    if ($result['success']) {
        echo "✅ SUCCESS: SMS API is working correctly!\n";
        echo "📊 Results:\n";
        echo "   - Successful sends: " . $result['data']['successful_sends'] . "\n";
        echo "   - Failed sends: " . $result['data']['failed_sends'] . "\n";
        
        if (isset($result['data']['location_info'])) {
            echo "   - Location: " . $result['data']['location_info']['formatted_address'] . "\n";
        }
    } else {
        echo "❌ FAILED: " . $result['message'] . "\n";
    }
    
} catch (Exception $e) {
    echo "❌ ERROR: " . $e->getMessage() . "\n";
}

echo "\n" . str_repeat("=", 40) . "\n";
echo "Test completed at " . date('Y-m-d H:i:s') . "\n";
?>

