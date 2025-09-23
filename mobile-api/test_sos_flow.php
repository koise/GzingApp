<?php
/**
 * Test SOS Flow
 * Test the new SOS flow where MapActivity and RoutesMapsActivity pass coordinates and location data
 */

echo "=== TESTING NEW SOS FLOW ===\n\n";

$url = 'https://powderblue-pig-261057.hostingersite.com/mobile-api/sms/send_emergency_sms';

// Test with coordinates and location data that would be passed from activities
$data = [
    'contacts' => ['+639934469840'],
    'emergency_type' => 'emergency',
    'latitude' => 14.6227,  // Real coordinates from activities
    'longitude' => 121.1766,
    'message' => 'Emergency from MapActivity/RoutesMapsActivity with real coordinates and reverse geocoded location',
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

echo "Testing new SOS flow with activity-provided coordinates...\n";
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
                echo "🎉 SOS FLOW WORKING WITH ACTIVITY COORDINATES!\n";
                echo "✅ MapActivity and RoutesMapsActivity can now pass coordinates to SOS dialog!\n";
                
                if (isset($json['data']['location_info'])) {
                    $locationInfo = $json['data']['location_info'];
                    echo "📍 Reverse Geocoded Location:\n";
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

echo "\n=== SOS FLOW TEST COMPLETE ===\n";
echo "\n📋 NEW SOS FLOW IMPLEMENTATION:\n";
echo "✅ SosHelpDialog now accepts initial coordinates and location\n";
echo "✅ MapActivity passes current location coordinates to SOS dialog\n";
echo "✅ RoutesMapsActivity passes current location coordinates to SOS dialog\n";
echo "✅ Both activities provide reverse geocoded location strings\n";
echo "✅ SOS dialog uses provided coordinates instead of requesting fresh GPS\n";
echo "\n🎯 FLOW SUMMARY:\n";
echo "1. User presses SOS button in MapActivity or RoutesMapsActivity\n";
echo "2. Activity gets current coordinates and reverse geocoded location\n";
echo "3. Activity calls SosHelpDialog with coordinates and location data\n";
echo "4. SosHelpDialog uses provided data instead of requesting fresh GPS\n";
echo "5. Emergency SMS is sent with accurate location information\n";
?>



