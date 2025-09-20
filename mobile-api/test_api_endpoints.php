<?php
/**
 * Test API Endpoints
 * Simple script to test the mobile API endpoints
 */

echo "Testing Mobile API Endpoints\n";
echo "============================\n\n";

// Base URL
$baseUrl = 'https://powderblue-pig-261057.hostingersite.com/mobile-api';

// Test endpoints
$endpoints = [
    'Health Check' => '/health',
    'API Info' => '/',
    'Routes' => '/routes',
    'Routes with Status' => '/routes?status=active'
];

foreach ($endpoints as $name => $endpoint) {
    echo "Testing: $name\n";
    echo "URL: $baseUrl$endpoint\n";
    
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $baseUrl . $endpoint);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Accept: application/json'
    ]);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    curl_close($ch);
    
    if ($error) {
        echo "CURL Error: $error\n";
    } else {
        echo "HTTP Code: $httpCode\n";
        if ($response) {
            $decoded = json_decode($response, true);
            if ($decoded) {
                echo "Response: " . json_encode($decoded, JSON_PRETTY_PRINT) . "\n";
            } else {
                echo "Raw Response: $response\n";
            }
        } else {
            echo "No response received\n";
        }
    }
    
    echo "\n" . str_repeat("-", 50) . "\n\n";
}

echo "API Testing Complete!\n";
?>

