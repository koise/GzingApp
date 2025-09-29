<?php
/**
 * Get User Profile Endpoint
 * GET /mobile-api/users/get_user_profile.php
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

// Set content type
header('Content-Type: application/json');

// Include required files
require_once '../config/database.php';
require_once '../includes/Response.php';
require_once '../includes/Validator.php';

try {
    // Only allow GET requests
    if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
        Response::methodNotAllowed();
    }
    
    // Get user ID from query parameter
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;
    
    if (!$userId) {
        Response::error('User ID is required', 400);
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get user profile data
    $userStmt = $conn->prepare("
        SELECT 
            id, first_name, last_name, email, username, phone_number, 
            role, status, created_at, last_login
        FROM users 
        WHERE id = ? AND status != 'deleted'
    ");
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch();
    
    if (!$user) {
        Response::error('User not found', 404);
    }
    
    // Get SOS contacts count
    $sosCountStmt = $conn->prepare("
        SELECT COUNT(*) as count 
        FROM sos_contacts 
        WHERE user_id = ?
    ");
    $sosCountStmt->execute([$userId]);
    $sosCount = $sosCountStmt->fetch()['count'];
    
    // Get navigation logs count
    $logsCountStmt = $conn->prepare("
        SELECT COUNT(*) as count 
        FROM navigation_activity_logs 
        WHERE user_id = ?
    ");
    $logsCountStmt->execute([$userId]);
    $logsCount = $logsCountStmt->fetch()['count'];
    
    // Get last activity
    $lastActivityStmt = $conn->prepare("
        SELECT created_at 
        FROM navigation_activity_logs 
        WHERE user_id = ? 
        ORDER BY created_at DESC 
        LIMIT 1
    ");
    $lastActivityStmt->execute([$userId]);
    $lastActivity = $lastActivityStmt->fetch();
    
    // Prepare user profile response
    $userProfile = [
        'id' => $user['id'],
        'first_name' => $user['first_name'],
        'last_name' => $user['last_name'],
        'email' => $user['email'],
        'username' => $user['username'],
        'phone_number' => $user['phone_number'],
        'role' => $user['role'],
        'status' => $user['status'],
        'created_at' => $user['created_at'],
        'last_login' => $user['last_login'],
        'sos_contacts_count' => $sosCount,
        'navigation_logs_count' => $logsCount,
        'last_activity' => $lastActivity ? $lastActivity['created_at'] : null
    ];
    
    // Return success response
    Response::success([
        'profile' => $userProfile
    ], 'User profile retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve user profile: ' . $e->getMessage());
}
?>

