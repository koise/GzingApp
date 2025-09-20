<?php
/**
 * Logout Endpoint
 * POST /auth/logout
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/SessionManager.php';

try {
    // Check if user is logged in
    if (!SessionManager::isLoggedIn()) {
        Response::error('No active session to logout', 400);
    }
    
    // Get user data before logout
    $userData = SessionManager::getUserData();
    
    // Initialize database for logging
    $db = new Database();
    $conn = $db->getConnection();
    
    // Log the logout activity
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'user_id' => $userData['id'],
        'username' => $userData['username'],
        'role' => $userData['role'],
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'user_agent' => $userAgent,
        'session_duration' => time() - ($_SESSION['login_time'] ?? time()),
        'logout_reason' => 'user_initiated',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'user_logout',
        'info',
        $userData['first_name'] . ' ' . $userData['last_name'],
        'User Logout',
        'User ' . $userData['first_name'] . ' ' . $userData['last_name'] . ' logged out',
        $userAgent,
        $logData
    ]);
    
    // Perform logout
    SessionManager::logout();
    
    // Return success response
    Response::success(null, 'Logout successful');
    
} catch (Exception $e) {
    Response::serverError('Logout failed: ' . $e->getMessage());
}
?>

