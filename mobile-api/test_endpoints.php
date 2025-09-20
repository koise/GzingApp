<?php
/**
 * Test API Endpoints
 */

echo "<h1>API Endpoint Tests</h1>";

$baseUrl = "https://powderblue-pig-261057.hostingersite.com/mobile-api";

// Test 1: Simple test endpoint
echo "<h2>Test 1: Simple Test Endpoint</h2>";
$testUrl = $baseUrl . "/test.php";
echo "URL: $testUrl<br>";
$response = file_get_contents($testUrl);
echo "Response: <pre>" . htmlspecialchars($response) . "</pre><br><br>";

// Test 2: Health check
echo "<h2>Test 2: Health Check</h2>";
$healthUrl = $baseUrl . "/health";
echo "URL: $healthUrl<br>";
$response = file_get_contents($healthUrl);
echo "Response: <pre>" . htmlspecialchars($response) . "</pre><br><br>";

// Test 3: Signup endpoint (direct)
echo "<h2>Test 3: Signup Endpoint (Direct)</h2>";
$signupUrl = $baseUrl . "/auth/signup.php";
echo "URL: $signupUrl<br>";

$postData = json_encode([
    'first_name' => 'Test',
    'last_name' => 'User',
    'email' => 'test@example.com',
    'username' => 'testuser',
    'password' => 'password123',
    'phone_number' => '+639123456789'
]);

$context = stream_context_create([
    'http' => [
        'method' => 'POST',
        'header' => 'Content-Type: application/json',
        'content' => $postData
    ]
]);

$response = file_get_contents($signupUrl, false, $context);
echo "Response: <pre>" . htmlspecialchars($response) . "</pre><br><br>";

// Test 4: Session check
echo "<h2>Test 4: Session Check</h2>";
$checkUrl = $baseUrl . "/auth/check.php";
echo "URL: $checkUrl<br>";
$response = file_get_contents($checkUrl);
echo "Response: <pre>" . htmlspecialchars($response) . "</pre><br><br>";

echo "<h2>Server Information</h2>";
echo "Server: " . $_SERVER['SERVER_NAME'] . "<br>";
echo "Document Root: " . $_SERVER['DOCUMENT_ROOT'] . "<br>";
echo "Script Name: " . $_SERVER['SCRIPT_NAME'] . "<br>";
echo "Request URI: " . $_SERVER['REQUEST_URI'] . "<br>";
?>

