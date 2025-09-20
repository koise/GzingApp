<?php
/**
 * Generate JWT Token for User 10
 * Local script to create a JWT token for testing
 */

// Simple JWT implementation for token generation
function base64UrlEncode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

function generateJWT($payload, $secret = 'your-secret-key') {
    $header = json_encode(['typ' => 'JWT', 'alg' => 'HS256']);
    $payload = json_encode($payload);
    
    $base64Header = base64UrlEncode($header);
    $base64Payload = base64UrlEncode($payload);
    
    $signature = hash_hmac('sha256', $base64Header . "." . $base64Payload, $secret, true);
    $base64Signature = base64UrlEncode($signature);
    
    return $base64Header . "." . $base64Payload . "." . $base64Signature;
}

// User 10 data
$userData = [
    'user_id' => 10,
    'username' => 'user10',
    'email' => 'user10@example.com',
    'first_name' => 'User',
    'last_name' => 'Ten',
    'phone' => '+1234567890',
    'created_at' => date('Y-m-d H:i:s'),
    'last_login' => date('Y-m-d H:i:s'),
    'iat' => time(),
    'exp' => time() + (24 * 60 * 60) // 24 hours
];

// Generate token
$token = generateJWT($userData);

echo "JWT Token for User 10:\n";
echo "=====================\n";
echo $token . "\n\n";

echo "User Data:\n";
echo "==========\n";
echo json_encode($userData, JSON_PRETTY_PRINT) . "\n\n";

echo "Usage in API calls:\n";
echo "===================\n";
echo "Authorization: Bearer " . $token . "\n\n";

echo "Test the API with this token:\n";
echo "=============================\n";
echo "curl -H \"Authorization: Bearer " . $token . "\" \\\n";
echo "     -H \"Content-Type: application/json\" \\\n";
echo "     -X GET https://powderblue-pig-261057.hostingersite.com/mobile-api/navigation_activity_logs/logs\n";
?>




