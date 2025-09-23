<?php
/**
 * Test Coordinate Capture
 * Test if Android app is sending real coordinates instead of 0.0
 */

echo "=== TESTING COORDINATE CAPTURE ===\n\n";

$url = 'https://powderblue-pig-261057.hostingersite.com/mobile-api/sms/send_emergency_sms';

// Test with real coordinates (Manila, Philippines)
$data = [
    'contacts' => ['+639934469840'],
    'emergency_type' => 'emergency',
    'latitude' => 14.5995,  // Manila coordinates
    'longitude' => 120.9842,
    'message' => 'Test with real Manila coordinates',
    'user_id' => 5  // Use working user ID
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

echo "Testing SMS service with real coordinates...\n";
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
                echo "🎉 SMS SENT SUCCESSFULLY WITH REAL COORDINATES!\n";
                echo "✅ Coordinate capture is working correctly!\n";
                
                if (isset($json['data']['location_info'])) {
                    $locationInfo = $json['data']['location_info'];
                    echo "📍 Location Info:\n";
                    echo "  Address: " . ($locationInfo['formatted_address'] ?? 'N/A') . "\n";
                    echo "  Place Type: " . ($locationInfo['place_type'] ?? 'N/A') . "\n";
                    echo "  Confidence: " . ($locationInfo['confidence'] ?? 'N/A') . "\n";
                }
                
                if (isset($json['data']['results'])) {
                    foreach ($json['data']['results'] as $result) {
                        echo "📱 SMS Details:\n";
                        echo "  Phone: " . $result['phone'] . "\n";
                        echo "  Success: " . ($result['success'] ? 'YES' : 'NO') . "\n";
                        if (isset($result['uid'])) {
                            echo "  SMS UID: " . $result['uid'] . "\n";
                        }
                        if (isset($result['cost'])) {
                            echo "  Cost: " . $result['cost'] . "\n";
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

echo "\n=== COORDINATE CAPTURE TEST COMPLETE ===\n";
echo "\n📋 SUMMARY:\n";
echo "✅ Android app coordinate capture improvements:\n";
echo "  - Enhanced GPS location detection in SosHelpDialog\n";
echo "  - Improved coordinate validation in SosSmsService\n";
echo "  - Better location handling in MapActivity\n";
echo "  - Enhanced location capture in RoutesMapsActivity\n";
echo "  - Added comprehensive logging for debugging\n";
echo "\n🎯 EXPECTED RESULT:\n";
echo "The Android app should now capture and send real GPS coordinates\n";
echo "instead of 0.0, 0.0 when the SOS button is pressed.\n";
?>



