<?php
/**
 * Direct Login Endpoint
 * POST /mobile-api/auth/login.php
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
require_once '../config/database.php';
require_once '../includes/Response.php';
require_once '../includes/Validator.php';
require_once '../includes/SessionManager.php';

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
    
    // Find user by email
    $stmt = $conn->prepare("
        SELECT id, first_name, last_name, email, username, password_hash, role, phone_number, status
        FROM users 
        WHERE email = ? AND deleted_at IS NULL
    ");
    $stmt->execute([$email]);
    $user = $stmt->fetch();
    
    if (!$user) {
        Response::error('Invalid email or password');
    }
    
    // Check if user is active
    if ($user['status'] !== 'active') {
        Response::error('Account is not active');
    }
    
    // Verify password
    if (!password_verify($password, $user['password_hash'])) {
        Response::error('Invalid email or password');
    }
    
    // Log the login
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'user_id' => $user['id'],
        'email' => $email,
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'user_login',
        'info',
        $user['first_name'] . ' ' . $user['last_name'],
        'User Login',
        'User logged in: ' . $user['username'],
        $userAgent,
        $logData
    ]);
    
    // Prepare user data for session
    $userData = [
        'id' => $user['id'],
        'email' => $user['email'],
        'username' => $user['username'],
        'first_name' => $user['first_name'],
        'last_name' => $user['last_name'],
        'role' => $user['role'],
        'phone_number' => $user['phone_number']
    ];
    
    // Create session
    SessionManager::login($userData);
    
    // Return success response
    Response::success([
        'user' => $userData,
        'session_id' => session_id()
    ], 'Login successful');
    
} catch (Exception $e) {
    Response::serverError('Login failed: ' . $e->getMessage());
}
?>

