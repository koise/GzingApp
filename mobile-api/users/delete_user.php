<?php
/**
 * Delete User Account Endpoint
 * POST /mobile-api/users/delete_user.php
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
    // Only allow POST requests
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        Response::methodNotAllowed();
    }
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input');
    }
    
    // Validate required fields
    $requiredFields = ['user_id', 'password'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $userId = intval($input['user_id']);
    $password = $input['password']; // Don't sanitize password as it may contain special characters
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get user data and verify password
    $userStmt = $conn->prepare("
        SELECT id, first_name, last_name, email, password_hash, status 
        FROM users 
        WHERE id = ? AND deleted_at IS NULL
    ");
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch();
    
    if (!$user) {
        Response::error('User not found', 404);
    }
    
    // Verify password
    if (!password_verify($password, $user['password_hash'])) {
        Response::error('Invalid password', 401);
    }
    
    // Check if user is already deleted
    if ($user['status'] === 'deleted') {
        Response::error('User account is already deleted');
    }
    
    // Soft delete user (set status to deleted and add deleted_at timestamp)
    $deleteStmt = $conn->prepare("
        UPDATE users 
        SET status = 'deleted', deleted_at = NOW(), updated_at = NOW()
        WHERE id = ?
    ");
    
    $deleteStmt->execute([$userId]);
    
    // Also soft delete related data
    $deleteSosStmt = $conn->prepare("
        UPDATE sos_contacts 
        SET deleted_at = NOW(), updated_at = NOW()
        WHERE user_id = ?
    ");
    $deleteSosStmt->execute([$userId]);
    
    $deleteLogsStmt = $conn->prepare("
        UPDATE navigation_activity_logs 
        SET deleted_at = NOW(), updated_at = NOW()
        WHERE user_id = ?
    ");
    $deleteLogsStmt->execute([$userId]);
    
    // Log the user deletion
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'deleted_user_id' => $userId,
        'deleted_user_email' => $user['email'],
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'user_delete',
        'warning',
        $user['first_name'] . ' ' . $user['last_name'],
        'User Account Deleted',
        'User account permanently deleted',
        $userAgent,
        $logData
    ]);
    
    // Return success response
    Response::success([
        'deleted_user_id' => $userId,
        'deleted_at' => date('Y-m-d H:i:s')
    ], 'User account deleted successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to delete user account: ' . $e->getMessage());
}
?>

