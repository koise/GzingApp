<?php
/**
 * Signup Endpoint
 * POST /auth/signup
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/Validator.php';
require_once '../../includes/SessionManager.php';

try {
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
    
    // Validate username (alphanumeric and underscore only, 3-20 characters)
    if (!preg_match('/^[a-zA-Z0-9_]{3,20}$/', $username)) {
        Response::error('Username must be 3-20 characters long and contain only letters, numbers, and underscores');
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
        INSERT INTO users (first_name, last_name, email, username, password_hash, phone_number, role, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, 'user', 'active', NOW(), NOW())
    ");
    
    $insertStmt->execute([
        $firstName,
        $lastName,
        $email,
        $username,
        $passwordHash,
        $phoneNumber
    ]);
    
    $userId = $conn->lastInsertId();
    
    // Log the registration
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'user_id' => $userId,
        'email' => $email,
        'username' => $username,
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'user_create',
        'info',
        $firstName . ' ' . $lastName,
        'User Registration',
        'New user registered: ' . $username,
        $userAgent,
        $logData
    ]);
    
    // Get the created user data
    $userStmt = $conn->prepare("
        SELECT id, first_name, last_name, email, username, role, phone_number
        FROM users WHERE id = ?
    ");
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch();
    
    // Create session
    $userData = [
        'id' => $user['id'],
        'email' => $user['email'],
        'username' => $user['username'],
        'first_name' => $user['first_name'],
        'last_name' => $user['last_name'],
        'role' => $user['role'],
        'phone_number' => $user['phone_number']
    ];
    
    SessionManager::login($userData);
    
    // Return success response
    Response::success([
        'user' => $user,
        'session_id' => session_id()
    ], 'Account created successfully');
    
} catch (Exception $e) {
    Response::serverError('Registration failed: ' . $e->getMessage());
}
?>

