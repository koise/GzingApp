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

// Get the request method and path
$method = $_SERVER['REQUEST_METHOD'];
$path = $_SERVER['REQUEST_URI'];

// Remove query string from path
$path = strtok($path, '?');

// Route the request based on method and path
switch ($method) {
    case 'POST':
        // Create new navigation history
        if (strpos($path, '/navigation-history') !== false) {
            include __DIR__ . '/create_navigation_history.php';
        } else {
            http_response_code(404);
            echo json_encode([
                'success' => false,
                'message' => 'Endpoint not found',
                'path' => $path,
                'method' => $method,
                'timestamp' => date('Y-m-d H:i:s')
            ]);
        }
        break;
        
    case 'GET':
        // Get navigation history by ID
        if (strpos($path, '/navigation-history/') !== false) {
            // Extract ID from path (e.g., /navigation-history/123)
            $path_parts = explode('/', trim($path, '/'));
            $id = end($path_parts);
            
            if (is_numeric($id)) {
                $_GET['id'] = $id;
                include __DIR__ . '/get_navigation_history_by_id.php';
            } else {
                http_response_code(400);
                echo json_encode([
                    'success' => false,
                    'message' => 'Invalid history ID',
                    'timestamp' => date('Y-m-d H:i:s')
                ]);
            }
        } else {
            http_response_code(404);
            echo json_encode([
                'success' => false,
                'message' => 'Endpoint not found',
                'path' => $path,
                'method' => $method,
                'timestamp' => date('Y-m-d H:i:s')
            ]);
        }
        break;
        
    default:
        http_response_code(405);
        echo json_encode([
            'success' => false,
            'message' => 'Method not allowed',
            'path' => $path,
            'method' => $method,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        break;
}
?>

