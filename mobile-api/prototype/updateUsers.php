<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Only allow GET and POST methods
if ($_SERVER['REQUEST_METHOD'] !== 'GET' && $_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed. Only GET and POST are supported.',
        'data' => null,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    exit();
}

// Database configuration
$host = 'localhost';
$dbname = 'u126959096_gzing_admin';
$username = 'u126959096_gzing_admin';
$password = 'X6v8M$U9;j';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Database connection failed: ' . $e->getMessage(),
        'data' => null,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    exit();
}

// Handle GET request - get user data for editing
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = isset($_GET['id']) ? (int)$_GET['id'] : null;
    
    if (!$user_id) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'User ID is required. Use: updateUsers.php?id=10',
            'data' => null,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    try {
        $stmt = $pdo->prepare("
            SELECT 
                id,
                first_name,
                last_name,
                email,
                username,
                phone_number,
                role,
                status,
                created_at,
                updated_at,
                last_login
            FROM users 
            WHERE id = ? AND status != 'deleted'
        ");
        
        $stmt->execute([$user_id]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if (!$user) {
            http_response_code(404);
            echo json_encode([
                'success' => false,
                'message' => 'User not found',
                'data' => null,
                'timestamp' => date('Y-m-d H:i:s')
            ]);
            exit();
        }
        
        // Format the response
        $response = [
            'success' => true,
            'message' => 'User data retrieved for editing',
            'data' => [
                'user' => [
                    'id' => (int)$user['id'],
                    'first_name' => $user['first_name'],
                    'last_name' => $user['last_name'],
                    'email' => $user['email'],
                    'username' => $user['username'],
                    'phone_number' => $user['phone_number'],
                    'role' => $user['role'],
                    'status' => $user['status'],
                    'created_at' => $user['created_at'],
                    'updated_at' => $user['updated_at'],
                    'last_login' => $user['last_login']
                ]
            ],
            'timestamp' => date('Y-m-d H:i:s')
        ];
        
        echo json_encode($response, JSON_PRETTY_PRINT);
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'Database error: ' . $e->getMessage(),
            'data' => null,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
    }
}

// Handle POST request - update user data
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Invalid JSON input',
            'data' => null,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    // Validate required fields
    $required_fields = ['user_id', 'first_name', 'last_name', 'email'];
    foreach ($required_fields as $field) {
        if (!isset($input[$field]) || empty($input[$field])) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => "Required field missing: $field",
                'data' => null,
                'timestamp' => date('Y-m-d H:i:s')
            ]);
            exit();
        }
    }
    
    $user_id = (int)$input['user_id'];
    $first_name = trim($input['first_name']);
    $last_name = trim($input['last_name']);
    $email = trim($input['email']);
    $username = isset($input['username']) ? trim($input['username']) : null;
    $phone_number = isset($input['phone_number']) ? trim($input['phone_number']) : null;
    $role = isset($input['role']) ? trim($input['role']) : 'user';
    
    // Validate email format
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Invalid email format',
            'data' => null,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    try {
        // Check if user exists
        $check_stmt = $pdo->prepare("SELECT id FROM users WHERE id = ? AND status != 'deleted'");
        $check_stmt->execute([$user_id]);
        
        if (!$check_stmt->fetch()) {
            http_response_code(404);
            echo json_encode([
                'success' => false,
                'message' => 'User not found',
                'data' => null,
                'timestamp' => date('Y-m-d H:i:s')
            ]);
            exit();
        }
        
        // Check if email is already taken by another user
        $email_check_stmt = $pdo->prepare("SELECT id FROM users WHERE email = ? AND id != ? AND status != 'deleted'");
        $email_check_stmt->execute([$email, $user_id]);
        
        if ($email_check_stmt->fetch()) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Email is already taken by another user',
                'data' => null,
                'timestamp' => date('Y-m-d H:i:s')
            ]);
            exit();
        }
        
        // Check if username is already taken by another user (if provided)
        if ($username) {
            $username_check_stmt = $pdo->prepare("SELECT id FROM users WHERE username = ? AND id != ? AND status != 'deleted'");
            $username_check_stmt->execute([$username, $user_id]);
            
            if ($username_check_stmt->fetch()) {
                http_response_code(400);
                echo json_encode([
                    'success' => false,
                    'message' => 'Username is already taken by another user',
                    'data' => null,
                    'timestamp' => date('Y-m-d H:i:s')
                ]);
                exit();
            }
        }
        
        // Update user data
        $update_stmt = $pdo->prepare("
            UPDATE users 
            SET 
                first_name = ?,
                last_name = ?,
                email = ?,
                username = ?,
                phone_number = ?,
                role = ?,
                updated_at = NOW()
            WHERE id = ?
        ");
        
        $update_stmt->execute([
            $first_name,
            $last_name,
            $email,
            $username,
            $phone_number,
            $role,
            $user_id
        ]);
        
        // Get updated user data
        $get_updated_stmt = $pdo->prepare("
            SELECT 
                id,
                first_name,
                last_name,
                email,
                username,
                phone_number,
                role,
                status,
                created_at,
                updated_at,
                last_login
            FROM users 
            WHERE id = ?
        ");
        
        $get_updated_stmt->execute([$user_id]);
        $updated_user = $get_updated_stmt->fetch(PDO::FETCH_ASSOC);
        
        $response = [
            'success' => true,
            'message' => 'User updated successfully',
            'data' => [
                'user' => [
                    'id' => (int)$updated_user['id'],
                    'first_name' => $updated_user['first_name'],
                    'last_name' => $updated_user['last_name'],
                    'email' => $updated_user['email'],
                    'username' => $updated_user['username'],
                    'phone_number' => $updated_user['phone_number'],
                    'role' => $updated_user['role'],
                    'status' => $updated_user['status'],
                    'created_at' => $updated_user['created_at'],
                    'updated_at' => $updated_user['updated_at'],
                    'last_login' => $updated_user['last_login']
                ]
            ],
            'timestamp' => date('Y-m-d H:i:s')
        ];
        
        echo json_encode($response, JSON_PRETTY_PRINT);
        
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'Database error: ' . $e->getMessage(),
            'data' => null,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
    }
}
?>

