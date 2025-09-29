<?php
/**
 * Update User Profile Endpoint
 * POST /mobile-api/users/update_user.php
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
    $requiredFields = ['user_id', 'first_name', 'last_name', 'email'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $userId = intval($input['user_id']);
    $firstName = Validator::sanitizeString($input['first_name']);
    $lastName = Validator::sanitizeString($input['last_name']);
    $email = Validator::sanitizeEmail($input['email']);
    $username = isset($input['username']) ? Validator::sanitizeString($input['username']) : null;
    $phoneNumber = isset($input['phone_number']) ? Validator::sanitizePhone($input['phone_number']) : null;
    $role = isset($input['role']) ? Validator::sanitizeString($input['role']) : 'user';
    
    // Validate email format
    if (!Validator::validateEmail($email)) {
        Response::error('Invalid email format');
    }
    
    // Validate phone number if provided
    if ($phoneNumber && !Validator::validatePhone($phoneNumber)) {
        Response::error('Invalid phone number format. Use +639XXXXXXXXX or 09XXXXXXXXX');
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Check if user exists
    $checkStmt = $conn->prepare("SELECT id FROM users WHERE id = ? AND status != 'deleted'");
    $checkStmt->execute([$userId]);
    if (!$checkStmt->fetch()) {
        Response::error('User not found', 404);
    }
    
    // Check if email is already taken by another user
    $emailCheckStmt = $conn->prepare("SELECT id FROM users WHERE email = ? AND id != ? AND status != 'deleted'");
    $emailCheckStmt->execute([$email, $userId]);
    if ($emailCheckStmt->fetch()) {
        Response::error('Email is already taken by another user');
    }
    
    // Check if username is already taken by another user (if provided)
    if ($username) {
        $usernameCheckStmt = $conn->prepare("SELECT id FROM users WHERE username = ? AND id != ? AND status != 'deleted'");
        $usernameCheckStmt->execute([$username, $userId]);
        if ($usernameCheckStmt->fetch()) {
            Response::error('Username is already taken by another user');
        }
    }
    
    // Update user
    $updateStmt = $conn->prepare("
        UPDATE users 
        SET first_name = ?, last_name = ?, email = ?, username = ?, phone_number = ?, role = ?, updated_at = NOW()
        WHERE id = ?
    ");
    
    $updateStmt->execute([
        $firstName,
        $lastName,
        $email,
        $username,
        $phoneNumber,
        $role,
        $userId
    ]);
    
    // Log the user update
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'user_id' => $userId,
        'updated_fields' => array_keys($input),
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'user_update',
        'info',
        $firstName . ' ' . $lastName,
        'User Profile Updated',
        'User profile updated successfully',
        $userAgent,
        $logData
    ]);
    
    // Get the updated user data
    $userStmt = $conn->prepare("
        SELECT id, first_name, last_name, email, username, phone_number, role, status, created_at, last_login
        FROM users WHERE id = ?
    ");
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch();
    
    // Return success response
    Response::success([
        'user' => $user
    ], 'User profile updated successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to update user profile: ' . $e->getMessage());
}
?>

