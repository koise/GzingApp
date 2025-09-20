<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

// Database configuration
$host = 'localhost';
$dbname = 'u126959096_gzing_admin';
$username = 'u126959096_gzing_admin';
$password = 'X6v8M$U9;j';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database connection failed: ' . $e->getMessage(),
        'data' => null
    ]);
    exit();
}

try {
    // Get all users
    $stmt = $pdo->query("
        SELECT 
            id,
            first_name,
            last_name,
            email,
            username,
            status,
            created_at
        FROM users 
        ORDER BY id ASC
    ");
    
    $users = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Check specifically for user ID 9
    $stmt9 = $pdo->prepare("
        SELECT 
            id,
            first_name,
            last_name,
            email,
            username,
            status,
            created_at,
            updated_at
        FROM users 
        WHERE id = 9
    ");
    
    $stmt9->execute();
    $user9 = $stmt9->fetch(PDO::FETCH_ASSOC);
    
    $response = [
        'success' => true,
        'message' => 'Database check completed',
        'data' => [
            'total_users' => count($users),
            'all_users' => $users,
            'user_9_exists' => $user9 !== false,
            'user_9_data' => $user9,
            'user_9_status' => $user9 ? $user9['status'] : 'not_found'
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ];
    
    echo json_encode($response, JSON_PRETTY_PRINT);
    
} catch (PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage(),
        'data' => null,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>
