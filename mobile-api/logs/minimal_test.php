<?php
/**
 * Minimal test endpoint to check basic functionality
 */

header('Content-Type: application/json');

// Test 1: Basic PHP functionality
echo json_encode([
    'success' => true,
    'message' => 'PHP is working',
    'timestamp' => date('Y-m-d H:i:s')
]);
?>

