<?php
/**
 * Test Emergency SMS API
 * This file demonstrates how to use the emergency SMS service
 */

// Test data
$test_data = [
    'user_id' => 10,
    'latitude' => 14.62270180,
    'longitude' => 121.17656790,
    'emergency_type' => 'emergency',
    'message' => 'Something went wrong, I need immediate help!',
    'contacts' => [
        '09123456789',  // Philippine format
        '63987654321',  // International format
        '+639123456789' // With + sign
    ]
];

// API endpoint
$api_url = 'http://localhost/mobile-api/endpoints/sms/send_emergency_sms.php';

echo "🧪 Testing Emergency SMS API\n";
echo "============================\n\n";

echo "📋 Test Data:\n";
echo "User ID: " . $test_data['user_id'] . "\n";
echo "Coordinates: " . $test_data['latitude'] . ", " . $test_data['longitude'] . "\n";
echo "Emergency Type: " . $test_data['emergency_type'] . "\n";
echo "Message: " . $test_data['message'] . "\n";
echo "Contacts: " . implode(', ', $test_data['contacts']) . "\n\n";

// Prepare the request
$options = [
    'http' => [
        'header' => "Content-type: application/json\r\n",
        'method' => 'POST',
        'content' => json_encode($test_data)
    ]
];

$context = stream_context_create($options);

echo "📤 Sending request to API...\n";

try {
    $response = file_get_contents($api_url, false, $context);
    
    if ($response === false) {
        throw new Exception('Failed to get response from API');
    }
    
    $result = json_decode($response, true);
    
    echo "📥 API Response:\n";
    echo json_encode($result, JSON_PRETTY_PRINT) . "\n\n";
    
    if ($result['success']) {
        echo "✅ SUCCESS: Emergency SMS sent successfully!\n";
        echo "📊 Statistics:\n";
        echo "   - Total contacts: " . $result['data']['total_contacts'] . "\n";
        echo "   - Successful sends: " . $result['data']['successful_sends'] . "\n";
        echo "   - Failed sends: " . $result['data']['failed_sends'] . "\n";
        
        if (isset($result['data']['location_info'])) {
            echo "   - Location: " . $result['data']['location_info']['formatted_address'] . "\n";
        }
        
        echo "\n📱 Individual Results:\n";
        foreach ($result['data']['results'] as $result_item) {
            $status = $result_item['success'] ? '✅' : '❌';
            echo "   $status " . $result_item['phone'] . ": " . $result_item['message'] . "\n";
        }
    } else {
        echo "❌ FAILED: " . $result['message'] . "\n";
    }
    
} catch (Exception $e) {
    echo "❌ ERROR: " . $e->getMessage() . "\n";
}

echo "\n" . str_repeat("=", 50) . "\n";
echo "Test completed at " . date('Y-m-d H:i:s') . "\n";
?>

