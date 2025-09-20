<?php
/**
 * Login Endpoint
 * POST /auth/login
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
    $requiredFields = ['email', 'password'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $email = Validator::sanitizeEmail($input['email']);
    $password = $input['password'];
    
    // Validate email format
    if (!Validator::validateEmail($email)) {
        Response::error('Invalid email format');
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Check if user exists
    $stmt = $conn->prepare("
        SELECT id, first_name, last_name, email, username, password_hash, role, status, phone_number
        FROM users 
        WHERE (email = ? OR username = ?) AND deleted_at IS NULL
    ");
    $stmt->execute([$email, $email]);
    $user = $stmt->fetch();
    
    if (!$user) {
        Response::error('Invalid email or password', 401);
    }
    
    // Check if account is active
    if ($user['status'] !== 'active') {
        Response::error('Account is not active. Please contact support.', 401);
    }
    
    // Verify password
    if (!password_verify($password, $user['password_hash'])) {
        Response::error('Invalid email or password', 401);
    }
    
    // Update last login
    $updateStmt = $conn->prepare("UPDATE users SET last_login = NOW() WHERE id = ?");
    $updateStmt->execute([$user['id']]);
    
    // Log the login activity
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'user_id' => $user['id'],
        'email' => $user['email'],
        'role' => $user['role'],
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'success' => true,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'user_login',
        'info',
        $user['first_name'] . ' ' . $user['last_name'],
        'User Login',
        'User ' . $user['username'] . ' logged in successfully',
        $userAgent,
        $logData
    ]);
    
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
        'user' => [
            'id' => $user['id'],
            'email' => $user['email'],
            'username' => $user['username'],
            'first_name' => $user['first_name'],
            'last_name' => $user['last_name'],
            'role' => $user['role'],
            'phone_number' => $user['phone_number']
        ],
        'session_id' => session_id()
    ], 'Login successful');
    
} catch (Exception $e) {
    Response::serverError('Login failed: ' . $e->getMessage());
}
?>

