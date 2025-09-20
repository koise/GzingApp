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
        'message' => 'Database connection failed',
        'data' => null,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    exit();
}

// Function to send JSON response
function sendResponse($success, $message, $data = null, $httpCode = 200) {
    http_response_code($httpCode);
    echo json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    exit();
}

// Only allow GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendResponse(false, 'Only GET method is allowed', null, 405);
}

// Get contact_id from query parameters
$contactId = isset($_GET['contact_id']) ? (int)$_GET['contact_id'] : null;

if (!$contactId) {
    sendResponse(false, 'contact_id parameter is required', null, 400);
}

try {
    // Get emergency contact details
    $contactQuery = "SELECT sc.id, sc.user_id, sc.name, sc.phone_number, sc.relationship, sc.is_primary, 
                            sc.created_at, sc.updated_at, u.first_name, u.last_name
                     FROM sos_contacts sc
                     JOIN users u ON sc.user_id = u.id
                     WHERE sc.id = ? AND sc.deleted_at IS NULL AND u.deleted_at IS NULL";
    $contactStmt = $pdo->prepare($contactQuery);
    $contactStmt->execute([$contactId]);
    $contact = $contactStmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$contact) {
        sendResponse(false, 'Emergency contact not found', null, 404);
    }
    
    // Format response
    $responseData = [
        'contact' => [
            'id' => $contact['id'],
            'user_id' => $contact['user_id'],
            'name' => $contact['name'],
            'phone_number' => $contact['phone_number'],
            'relationship' => $contact['relationship'],
            'is_primary' => (bool)$contact['is_primary'],
            'created_at' => $contact['created_at'],
            'updated_at' => $contact['updated_at']
        ],
        'user' => [
            'id' => $contact['user_id'],
            'name' => $contact['first_name'] . ' ' . $contact['last_name']
        ]
    ];
    
    sendResponse(true, 'Emergency contact retrieved successfully', $responseData);
    
} catch (PDOException $e) {
    error_log("Database error in getEmergencyContact.php: " . $e->getMessage());
    sendResponse(false, 'Database error occurred', null, 500);
} catch (Exception $e) {
    error_log("General error in getEmergencyContact.php: " . $e->getMessage());
    sendResponse(false, 'An error occurred while retrieving emergency contact', null, 500);
}
?>

