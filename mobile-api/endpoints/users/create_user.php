<?php
/**
 * Create User Endpoint
 * POST /users
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/Validator.php';
require_once '../../includes/SessionManager.php';

try {
    // Require admin or moderator role
    SessionManager::requireRole('moderator');
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input');
    }
    
    // Validate required fields
    $requiredFields = ['first_name', 'last_name', 'email', 'username', 'password'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $firstName = Validator::sanitizeString($input['first_name']);
    $lastName = Validator::sanitizeString($input['last_name']);
    $email = Validator::sanitizeEmail($input['email']);
    $username = Validator::sanitizeString($input['username']);
    $password = $input['password'];
    $phoneNumber = isset($input['phone_number']) ? Validator::sanitizePhone($input['phone_number']) : null;
    $role = isset($input['role']) ? Validator::sanitizeString($input['role']) : 'user';
    $status = isset($input['status']) ? Validator::sanitizeString($input['status']) : 'active';
    $notes = isset($input['notes']) ? Validator::sanitizeString($input['notes']) : null;
    
    // Validate email format
    if (!Validator::validateEmail($email)) {
        Response::error('Invalid email format');
    }
    
    // Validate password strength
    if (!Validator::validatePassword($password)) {
        Response::error('Password must be at least 6 characters long');
    }
    
    // Validate phone number if provided
    if ($phoneNumber && !Validator::validatePhone($phoneNumber)) {
        Response::error('Invalid phone number format. Use +639XXXXXXXXX or 09XXXXXXXXX');
    }
    
    // Validate username
    if (!preg_match('/^[a-zA-Z0-9_]{3,20}$/', $username)) {
        Response::error('Username must be 3-20 characters long and contain only letters, numbers, and underscores');
    }
    
    // Validate role
    if (!in_array($role, ['user', 'moderator', 'admin'])) {
        Response::error('Invalid role. Must be user, moderator, or admin');
    }
    
    // Validate status
    if (!in_array($status, ['active', 'inactive', 'suspended'])) {
        Response::error('Invalid status. Must be active, inactive, or suspended');
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Check if email already exists
    $emailStmt = $conn->prepare("SELECT id FROM users WHERE email = ? AND deleted_at IS NULL");
    $emailStmt->execute([$email]);
    if ($emailStmt->fetch()) {
        Response::error('Email address is already registered');
    }
    
    // Check if username already exists
    $usernameStmt = $conn->prepare("SELECT id FROM users WHERE username = ? AND deleted_at IS NULL");
    $usernameStmt->execute([$username]);
    if ($usernameStmt->fetch()) {
        Response::error('Username is already taken');
    }
    
    // Hash password
    $passwordHash = password_hash($password, PASSWORD_DEFAULT);
    
    // Insert new user
    $insertStmt = $conn->prepare("
        INSERT INTO users (first_name, last_name, email, username, password_hash, phone_number, role, status, notes, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
    ");
    
    $insertStmt->execute([
        $firstName,
        $lastName,
        $email,
        $username,
        $passwordHash,
        $phoneNumber,
        $role,
        $status,
        $notes
    ]);
    
    $userId = $conn->lastInsertId();
    
    // Log the user creation
    $currentUser = SessionManager::getUserData();
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'created_user_id' => $userId,
        'created_by' => $currentUser['id'],
        'email' => $email,
        'username' => $username,
        'role' => $role,
        'status' => $status,
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'user_create',
        'info',
        $currentUser['first_name'] . ' ' . $currentUser['last_name'],
        'User Created',
        'User ' . $username . ' created by ' . $currentUser['username'],
        $userAgent,
        $logData
    ]);
    
    // Get the created user data
    $userStmt = $conn->prepare("
        SELECT id, first_name, last_name, email, username, role, status, phone_number, notes, created_at, updated_at
        FROM users WHERE id = ?
    ");
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch();
    
    // Return success response
    Response::success([
        'user' => $user
    ], 'User created successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to create user: ' . $e->getMessage());
}
?>

