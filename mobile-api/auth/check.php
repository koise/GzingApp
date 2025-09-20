<?php
/**
 * Direct Session Check Endpoint
 * GET /mobile-api/auth/check.php
 */

// Enable CORS for mobile app
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
header('Access-Control-Allow-Credentials: true');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Start session
session_start();

// Set content type
header('Content-Type: application/json');

// Include required files
require_once '../config/database.php';
require_once '../includes/Response.php';
require_once '../includes/SessionManager.php';

try {
    // Only allow GET requests
    if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
        Response::methodNotAllowed();
    }
    
    // Check if user is logged in
    if (SessionManager::isLoggedIn()) {
        $userData = SessionManager::getUserData();
        
        // Return user data
        Response::success([
            'user' => $userData,
            'session_id' => session_id(),
            'session_active' => true
        ], 'Session is valid');
    } else {
        Response::error('No active session', 401, [
            'session_active' => false
        ]);
    }
    
} catch (Exception $e) {
    Response::serverError('Session check failed: ' . $e->getMessage());
}
?>
