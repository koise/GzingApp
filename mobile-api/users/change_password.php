<?php
/**
 * Change User Password Endpoint
 * POST /mobile-api/users/change_password.php
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
    $requiredFields = ['user_id', 'current_password', 'new_password'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $userId = intval($input['user_id']);
    $currentPassword = $input['current_password'];
    $newPassword = $input['new_password'];
    
    // Validate new password strength
    if (strlen($newPassword) < 6) {
        Response::error('New password must be at least 6 characters long');
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get user data and verify current password
    $userStmt = $conn->prepare("
        SELECT id, first_name, last_name, email, password_hash 
        FROM users 
        WHERE id = ? AND deleted_at IS NULL
    ");
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch();
    
    if (!$user) {
        Response::error('User not found', 404);
    }
    
    // Verify current password
    if (!password_verify($currentPassword, $user['password_hash'])) {
        Response::error('Current password is incorrect', 401);
    }
    
    // Hash new password
    $newPasswordHash = password_hash($newPassword, PASSWORD_DEFAULT);
    
    // Update password
    $updateStmt = $conn->prepare("
        UPDATE users 
        SET password_hash = ?, updated_at = NOW()
        WHERE id = ?
    ");
    
    $updateStmt->execute([$newPasswordHash, $userId]);
    
    // Log the password change
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'user_id' => $userId,
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'password_change',
        'info',
        $user['first_name'] . ' ' . $user['last_name'],
        'Password Changed',
        'User password changed successfully',
        $userAgent,
        $logData
    ]);
    
    // Return success response
    Response::success([
        'user_id' => $userId,
        'password_changed_at' => date('Y-m-d H:i:s')
    ], 'Password changed successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to change password: ' . $e->getMessage());
}
?>

