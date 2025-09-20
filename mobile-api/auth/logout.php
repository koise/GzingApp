<?php
/**
 * Direct Logout Endpoint
 * POST /mobile-api/auth/logout.php
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
require_once '../includes/Response.php';
require_once '../includes/SessionManager.php';

try {
    // Only allow POST requests
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        Response::methodNotAllowed();
    }
    
    // Destroy session
    SessionManager::logout();
    
    // Return success response
    Response::success(null, 'Logged out successfully');
    
} catch (Exception $e) {
    Response::serverError('Logout failed: ' . $e->getMessage());
}
?>

