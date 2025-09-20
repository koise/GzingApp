<?php
/**
 * Navigation API Router
 * Handles all navigation-related endpoints
 */

// Enable CORS
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Get the request method and path
$method = $_SERVER['REQUEST_METHOD'];
$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$pathParts = explode('/', trim($path, '/'));

// Remove 'mobile-api/endpoints/navigation' from path
$pathParts = array_slice($pathParts, 3);

// Route the request
try {
    switch ($method) {
        case 'POST':
            if (empty($pathParts) || $pathParts[0] === '') {
                // POST /navigation - Create new navigation log
                require_once 'create_navigation_log.php';
            } elseif ($pathParts[0] === 'stop') {
                // POST /navigation/stop - Stop navigation
                require_once 'stop_navigation.php';
            } else {
                http_response_code(404);
                echo json_encode([
                    'success' => false,
                    'message' => 'Navigation endpoint not found',
                    'data' => null,
                    'timestamp' => date('Y-m-d H:i:s')
                ]);
            }
            break;
            
        case 'GET':
            if (empty($pathParts) || $pathParts[0] === '') {
                // GET /navigation - Get navigation logs
                require_once 'get_navigation_logs.php';
            } elseif ($pathParts[0] === 'stats') {
                // GET /navigation/stats - Get navigation statistics
                require_once 'get_navigation_stats.php';
            } elseif (is_numeric($pathParts[0])) {
                // GET /navigation/{log_id} - Get specific navigation log detail
                require_once 'get_navigation_log_detail.php';
            } else {
                http_response_code(404);
                echo json_encode([
                    'success' => false,
                    'message' => 'Navigation endpoint not found',
                    'data' => null,
                    'timestamp' => date('Y-m-d H:i:s')
                ]);
            }
            break;
            
        case 'PUT':
            if (is_numeric($pathParts[0])) {
                // PUT /navigation/{log_id} - Update navigation log
                require_once 'update_navigation_log.php';
            } else {
                http_response_code(404);
                echo json_encode([
                    'success' => false,
                    'message' => 'Navigation endpoint not found',
                    'data' => null,
                    'timestamp' => date('Y-m-d H:i:s')
                ]);
            }
            break;
            
        default:
            http_response_code(405);
            echo json_encode([
                'success' => false,
                'message' => 'Method not allowed',
                'data' => null,
                'timestamp' => date('Y-m-d H:i:s')
            ]);
            break;
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Internal server error: ' . $e->getMessage(),
        'data' => null,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>

