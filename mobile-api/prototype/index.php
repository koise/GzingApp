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

$response = [
    'success' => true,
    'message' => 'Prototype API - No Authentication Required',
    'data' => [
        'available_endpoints' => [
            [
                'endpoint' => 'getuser',
                'method' => 'GET',
                'description' => 'Fetch user data by ID',
                'example' => 'GET /getuser?id=10',
                'parameters' => [
                    'id' => 'User ID (required)'
                ]
            ],
            [
                'endpoint' => 'getuser',
                'method' => 'POST',
                'description' => 'Create new user',
                'example' => 'POST /getuser',
                'body' => [
                    'action' => 'create',
                    'first_name' => 'John',
                    'last_name' => 'Doe',
                    'email' => 'john@example.com',
                    'password' => 'password123',
                    'username' => 'johndoe (optional)',
                    'phone_number' => '+1234567890 (optional)'
                ]
            ],
            [
                'endpoint' => 'updateUsers',
                'method' => 'GET',
                'description' => 'Get user data for editing',
                'example' => 'GET /updateUsers?id=10',
                'parameters' => [
                    'id' => 'User ID (required)'
                ]
            ],
            [
                'endpoint' => 'updateUsers',
                'method' => 'POST',
                'description' => 'Update user data',
                'example' => 'POST /updateUsers',
                'body' => [
                    'user_id' => 10,
                    'first_name' => 'John',
                    'last_name' => 'Doe',
                    'email' => 'john@example.com',
                    'username' => 'johndoe (optional)',
                    'phone_number' => '+1234567890 (optional)',
                    'role' => 'user (optional)'
                ]
            ]
        ],
        'base_url' => 'https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/',
        'database' => 'u126959096_gzing_admin',
        'supported_methods' => ['GET', 'POST'],
        'authentication' => 'None required for prototyping',
        'cors_enabled' => true
    ],
    'timestamp' => date('Y-m-d H:i:s')
];

echo json_encode($response, JSON_PRETTY_PRINT);
?>
