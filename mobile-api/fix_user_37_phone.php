<?php
/**
 * Fix User 37 Phone Number
 * Add a phone number to user ID 37
 */

echo "=== FIXING USER 37 PHONE NUMBER ===\n\n";

$url = 'https://powderblue-pig-261057.hostingersite.com/mobile-api/users/update_user.php';

// Update user 37 with a phone number
$data = [
    'user_id' => 37,
    'phone_number' => '+639171234567'
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

echo "Updating user 37 with phone number: +639171234567\n";
echo "URL: $url\n";
echo "Data: " . json_encode($data) . "\n\n";

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
                echo "🎉 USER 37 PHONE NUMBER UPDATED SUCCESSFULLY!\n";
                echo "✅ Phone number added: +639171234567\n";
                echo "✅ User 37 should now work with SMS service\n";
            } else {
                echo "❌ UPDATE FAILED\n";
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

echo "\n=== UPDATE COMPLETE ===\n";
?>



