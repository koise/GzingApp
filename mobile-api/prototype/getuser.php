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

// Handle GET request - fetch user data
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = isset($_GET['id']) ? (int)$_GET['id'] : null;
    
    if (!$user_id) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'User ID is required. Use: getuser.php?id=10',
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
            'message' => 'User data retrieved successfully',
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

// Handle POST request - create or update user data
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
    
    $action = isset($input['action']) ? $input['action'] : 'create';
    
    if ($action === 'create') {
        // Create new user
        $required_fields = ['first_name', 'last_name', 'email', 'password'];
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
        
        try {
            $hashed_password = password_hash($input['password'], PASSWORD_DEFAULT);
            
            $stmt = $pdo->prepare("
                INSERT INTO users (
                    first_name, last_name, email, username, phone_number, 
                    password, role, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, 'user', 'active', NOW(), NOW())
            ");
            
            $stmt->execute([
                $input['first_name'],
                $input['last_name'],
                $input['email'],
                $input['username'] ?? null,
                $input['phone_number'] ?? null,
                $hashed_password
            ]);
            
            $new_user_id = $pdo->lastInsertId();
            
            echo json_encode([
                'success' => true,
                'message' => 'User created successfully',
                'data' => [
                    'user_id' => (int)$new_user_id,
                    'email' => $input['email']
                ],
                'timestamp' => date('Y-m-d H:i:s')
            ], JSON_PRETTY_PRINT);
            
        } catch (PDOException $e) {
            http_response_code(500);
            echo json_encode([
                'success' => false,
                'message' => 'Database error: ' . $e->getMessage(),
                'data' => null,
                'timestamp' => date('Y-m-d H:i:s')
            ]);
        }
    } else {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Invalid action. Use "create" for creating users.',
            'data' => null,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
    }
}
?>
