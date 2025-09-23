<?php
/**
 * Test Different Users
 * Test SMS service with different user IDs to find one with phone number
 */

echo "=== TESTING DIFFERENT USER IDs ===\n\n";

$url = 'https://powderblue-pig-261057.hostingersite.com/mobile-api/sms/send_emergency_sms';

// Test with different user IDs
$userIds = [1, 2, 3, 4, 5, 10, 20, 30, 35, 36, 38, 39, 40];

foreach ($userIds as $userId) {
    echo "Testing User ID: $userId\n";
    
    $data = [
        'contacts' => ['+639171234567'],
        'emergency_type' => 'emergency',
        'latitude' => 14.6760,
        'longitude' => 121.0437,
        'message' => 'Test emergency message',
        'user_id' => $userId
    ];
    
    $options = [
        'http' => [
            'header' => "Content-Type: application/json\r\n",
            'method' => 'POST',
            'content' => json_encode($data),
            'timeout' => 10
        ]
    ];
    
    $context = stream_context_create($options);
    
    try {
        $response = file_get_contents($url, false, $context);
        
        if ($response === false) {
            echo "  ❌ Connection failed\n";
            continue;
        }
        
        $json = json_decode($response, true);
        
        if (json_last_error() === JSON_ERROR_NONE) {
            if (isset($json['success']) && $json['success'] === true) {
                echo "  ✅ SUCCESS! User $userId has phone number\n";
                echo "  Message: " . $json['message'] . "\n";
                if (isset($json['data']['results'])) {
                    foreach ($json['data']['results'] as $result) {
                        echo "  SMS Result: " . ($result['success'] ? 'SUCCESS' : 'FAILED') . "\n";
                        if (isset($result['uid'])) {
                            echo "  SMS UID: " . $result['uid'] . "\n";
                        }
                    }
                }
                echo "\n🎉 FOUND WORKING USER ID: $userId\n";
                break;
            } else {
                $message = $json['message'] ?? 'Unknown error';
                if (strpos($message, 'phone number is missing') !== false) {
                    echo "  ❌ No phone number\n";
                } elseif (strpos($message, 'User not found') !== false) {
                    echo "  ❌ User not found\n";
                } else {
                    echo "  ❌ Error: $message\n";
                }
            }
        } else {
            echo "  ❌ Invalid JSON response\n";
        }
    } catch (Exception $e) {
        echo "  ❌ Exception: " . $e->getMessage() . "\n";
    }
    
    echo "\n";
}

echo "=== TEST COMPLETE ===\n";
?>



