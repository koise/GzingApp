<?php
/**
 * Simple API Test Script
 * Test the session-based authentication endpoints
 */

// Test configuration
$baseUrl = 'http://localhost/mobile-api';
$testEmail = 'test@example.com';
$testPassword = 'password123';

echo "=== Gzing Mobile API Test ===\n\n";

// Function to make HTTP requests
function makeRequest($url, $method = 'GET', $data = null) {
    $ch = curl_init();
    
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_COOKIEJAR, 'cookies.txt');
    curl_setopt($ch, CURLOPT_COOKIEFILE, 'cookies.txt');
    
    if ($method === 'POST') {
        curl_setopt($ch, CURLOPT_POST, true);
        if ($data) {
            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
            curl_setopt($ch, CURLOPT_HTTPHEADER, [
                'Content-Type: application/json',
                'Content-Length: ' . strlen(json_encode($data))
            ]);
        }
    }
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    return [
        'code' => $httpCode,
        'body' => $response
    ];
}

// Test 1: Health Check
echo "1. Testing Health Check...\n";
$response = makeRequest($baseUrl . '/health');
echo "Status: " . $response['code'] . "\n";
echo "Response: " . $response['body'] . "\n\n";

// Test 2: API Info
echo "2. Testing API Info...\n";
$response = makeRequest($baseUrl . '/info');
echo "Status: " . $response['code'] . "\n";
echo "Response: " . $response['body'] . "\n\n";

// Test 3: Signup
echo "3. Testing User Signup...\n";
$signupData = [
    'first_name' => 'Test',
    'last_name' => 'User',
    'email' => $testEmail,
    'username' => 'testuser',
    'password' => $testPassword,
    'phone_number' => '+639123456789'
];
$response = makeRequest($baseUrl . '/auth/signup', 'POST', $signupData);
echo "Status: " . $response['code'] . "\n";
echo "Response: " . $response['body'] . "\n\n";

// Test 4: Login
echo "4. Testing User Login...\n";
$loginData = [
    'email' => $testEmail,
    'password' => $testPassword
];
$response = makeRequest($baseUrl . '/auth/login', 'POST', $loginData);
echo "Status: " . $response['code'] . "\n";
echo "Response: " . $response['body'] . "\n\n";

// Test 5: Session Check
echo "5. Testing Session Check...\n";
$response = makeRequest($baseUrl . '/auth/check');
echo "Status: " . $response['code'] . "\n";
echo "Response: " . $response['body'] . "\n\n";

// Test 6: Get Routes (requires authentication)
echo "6. Testing Get Routes (authenticated)...\n";
$response = makeRequest($baseUrl . '/routes');
echo "Status: " . $response['code'] . "\n";
echo "Response: " . $response['body'] . "\n\n";

// Test 7: Get SOS Contacts (requires authentication)
echo "7. Testing Get SOS Contacts (authenticated)...\n";
$response = makeRequest($baseUrl . '/sos-contacts');
echo "Status: " . $response['code'] . "\n";
echo "Response: " . $response['body'] . "\n\n";

// Test 8: Logout
echo "8. Testing User Logout...\n";
$response = makeRequest($baseUrl . '/auth/logout', 'POST');
echo "Status: " . $response['code'] . "\n";
echo "Response: " . $response['body'] . "\n\n";

// Test 9: Session Check After Logout
echo "9. Testing Session Check After Logout...\n";
$response = makeRequest($baseUrl . '/auth/check');
echo "Status: " . $response['code'] . "\n";
echo "Response: " . $response['body'] . "\n\n";

echo "=== Test Complete ===\n";

// Clean up
if (file_exists('cookies.txt')) {
    unlink('cookies.txt');
}
?>

