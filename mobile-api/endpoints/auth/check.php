<?php
/**
 * Session Check Endpoint
 * GET /auth/check
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/SessionManager.php';

try {
    // Check if user is logged in
    if (!SessionManager::isLoggedIn()) {
        Response::error('No active session', 401);
    }
    
    // Check if session is expired
    if (SessionManager::isSessionExpired()) {
        SessionManager::logout();
        Response::error('Session expired', 401);
    }
    
    // Update last activity
    SessionManager::updateActivity();
    
    // Get user data
    $userData = SessionManager::getUserData();
    
    // Initialize database to get fresh user data
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get updated user information
    $stmt = $conn->prepare("
        SELECT id, first_name, last_name, email, username, role, status, phone_number, last_login, created_at
        FROM users 
        WHERE id = ? AND deleted_at IS NULL
    ");
    $stmt->execute([$userData['id']]);
    $user = $stmt->fetch();
    
    if (!$user) {
        // User was deleted, logout
        SessionManager::logout();
        Response::error('User account not found', 401);
    }
    
    if ($user['status'] !== 'active') {
        // User account is not active, logout
        SessionManager::logout();
        Response::error('Account is not active', 401);
    }
    
    // Return user data
    Response::success([
        'user' => [
            'id' => $user['id'],
            'first_name' => $user['first_name'],
            'last_name' => $user['last_name'],
            'email' => $user['email'],
            'username' => $user['username'],
            'role' => $user['role'],
            'status' => $user['status'],
            'phone_number' => $user['phone_number'],
            'last_login' => $user['last_login'],
            'created_at' => $user['created_at']
        ],
        'session_id' => session_id(),
        'session_active' => true
    ], 'Session is valid');
    
} catch (Exception $e) {
    Response::serverError('Session check failed: ' . $e->getMessage());
}
?>

