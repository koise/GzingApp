<?php
/**
 * Test User 5 SMS Service
 * Test SMS service with User ID 5 (which we know works)
 */

echo "=== TESTING USER 5 SMS SERVICE ===\n\n";

$url = 'https://powderblue-pig-261057.hostingersite.com/mobile-api/sms/send_emergency_sms';

$data = [
    'contacts' => ['+639934469840'],
    'emergency_type' => 'emergency',
    'latitude' => 14.6760,
    'longitude' => 121.0437,
    'message' => 'Test emergency with User 5',
    'user_id' => 5
];

$options = [
    'http' => [
        'header' => "Content-Type: application/json\r\n",
        'method' => 'POST',
        'content' => json_encode($data),
        'timeout' => 30
    ]
];

$context = stream_context_create($options);

echo "Testing SMS service with User ID 5...\n";
echo "URL: $url\n";
echo "Data: " . json_encode($data, JSON_PRETTY_PRINT) . "\n\n";

try {
    $response = file_get_contents($url, false, $context);
    
    if ($response === false) {
        echo "❌ FAILED: Could not connect to server\n";
        echo "Error: " . error_get_last()['message'] . "\n";
    } else {
        echo "✅ SUCCESS: Server response received\n";
        echo "Response Length: " . strlen($response) . " bytes\n";
        echo "Response Content:\n";
        echo "--- START RESPONSE ---\n";
        echo $response;
        echo "\n--- END RESPONSE ---\n\n";
        
        // Check if response is valid JSON
        $json = json_decode($response, true);
        if (json_last_error() === JSON_ERROR_NONE) {
            echo "✅ Valid JSON response\n";
            
            if (isset($json['success']) && $json['success'] === true) {
                echo "🎉 SMS SENT SUCCESSFULLY!\n";
                echo "✅ User 5 has phone number and SMS works!\n";
                
                if (isset($json['data']['results'])) {
                    foreach ($json['data']['results'] as $result) {
                        echo "Phone: " . $result['phone'] . "\n";
                        echo "Success: " . ($result['success'] ? 'YES' : 'NO') . "\n";
                        if (isset($result['uid'])) {
                            echo "SMS UID: " . $result['uid'] . "\n";
                        }
                        if (isset($result['cost'])) {
                            echo "Cost: " . $result['cost'] . "\n";
                        }
                    }
                }
            } else {
                echo "❌ SMS FAILED\n";
                echo "Error: " . ($json['message'] ?? 'Unknown error') . "\n";
            }
        } else {
            echo "❌ Invalid JSON response\n";
            echo "JSON Error: " . json_last_error_msg() . "\n";
            echo "First 200 characters: " . substr($response, 0, 200) . "\n";
        }
    }
} catch (Exception $e) {
    echo "❌ EXCEPTION: " . $e->getMessage() . "\n";
}

echo "\n=== TEST COMPLETE ===\n";
?>



