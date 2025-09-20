<?php
/**
 * Landmarks Endpoint Index
 * Handles landmarks-related API requests
 */

require_once __DIR__ . '/../../includes/Response.php';

// Get the request method
$requestMethod = $_SERVER['REQUEST_METHOD'];

// Route the request based on method
switch ($requestMethod) {
    case 'GET':
        // GET /landmarks - Get all landmarks
        require_once __DIR__ . '/get_landmarks.php';
        break;
        
    default:
        Response::methodNotAllowed('Only GET method is allowed for landmarks endpoint');
        break;
}
?>
