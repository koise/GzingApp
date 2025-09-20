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

$debug_info = [
    'request_method' => $_SERVER['REQUEST_METHOD'],
    'query_string' => $_SERVER['QUERY_STRING'] ?? 'none',
    'get_params' => $_GET,
    'database_config' => [
        'host' => $host,
        'dbname' => $dbname,
        'username' => $username
    ]
];

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $debug_info['database_connection'] = 'success';
} catch (PDOException $e) {
    $debug_info['database_connection'] = 'failed: ' . $e->getMessage();
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Database connection failed: ' . $e->getMessage(),
        'data' => null,
        'debug_info' => $debug_info,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    exit();
}

// Handle GET request - fetch user data
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $user_id = isset($_GET['id']) ? (int)$_GET['id'] : null;
    
    $debug_info['user_id_requested'] = $user_id;
    $debug_info['user_id_type'] = gettype($user_id);
    
    if (!$user_id) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'User ID is required. Use: getuser_debug.php?id=9',
            'data' => null,
            'debug_info' => $debug_info,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    try {
        // First, let's check if the user exists at all (including deleted)
        $stmt_all = $pdo->prepare("
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
        
        $stmt_all->execute([$user_id]);
        $user_all = $stmt_all->fetch(PDO::FETCH_ASSOC);
        
        $debug_info['user_exists_in_db'] = $user_all !== false;
        $debug_info['user_status'] = $user_all ? $user_all['status'] : 'not_found';
        
        // Now check with the original query (excluding deleted)
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
        
        $debug_info['user_found_with_filter'] = $user !== false;
        
        if (!$user) {
            http_response_code(404);
            echo json_encode([
                'success' => false,
                'message' => 'User not found or has deleted status',
                'data' => null,
                'debug_info' => $debug_info,
                'user_data_all_statuses' => $user_all,
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
            'debug_info' => $debug_info,
            'timestamp' => date('Y-m-d H:i:s')
        ];
        
        echo json_encode($response, JSON_PRETTY_PRINT);
        
    } catch (PDOException $e) {
        $debug_info['database_error'] = $e->getMessage();
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'message' => 'Database error: ' . $e->getMessage(),
            'data' => null,
            'debug_info' => $debug_info,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
    }
}
?>
