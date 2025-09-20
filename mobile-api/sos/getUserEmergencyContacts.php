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

// Get user_id from query parameters
$userId = isset($_GET['user_id']) ? (int)$_GET['user_id'] : null;

if (!$userId) {
    sendResponse(false, 'user_id parameter is required', null, 400);
}

try {
    // Check if user exists
    $userQuery = "SELECT id, first_name, last_name FROM users WHERE id = ? AND deleted_at IS NULL";
    $userStmt = $pdo->prepare($userQuery);
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$user) {
        sendResponse(false, 'User not found', null, 404);
    }
    
    // Get emergency contacts for the user
    $contactsQuery = "SELECT id, user_id, name, phone_number, relationship, is_primary, created_at, updated_at 
                      FROM sos_contacts 
                      WHERE user_id = ? AND deleted_at IS NULL 
                      ORDER BY is_primary DESC, created_at ASC";
    $contactsStmt = $pdo->prepare($contactsQuery);
    $contactsStmt->execute([$userId]);
    $contacts = $contactsStmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Get total count
    $countQuery = "SELECT COUNT(*) as total FROM sos_contacts WHERE user_id = ? AND deleted_at IS NULL";
    $countStmt = $pdo->prepare($countQuery);
    $countStmt->execute([$userId]);
    $count = $countStmt->fetch(PDO::FETCH_ASSOC)['total'];
    
    // Format response
    $responseData = [
        'user' => [
            'id' => $user['id'],
            'name' => $user['first_name'] . ' ' . $user['last_name']
        ],
        'contacts' => $contacts,
        'total_contacts' => (int)$count,
        'primary_contact' => null
    ];
    
    // Find primary contact
    foreach ($contacts as $contact) {
        if ($contact['is_primary'] == 1) {
            $responseData['primary_contact'] = $contact;
            break;
        }
    }
    
    sendResponse(true, 'Emergency contacts retrieved successfully', $responseData);
    
} catch (PDOException $e) {
    error_log("Database error in getUserEmergencyContacts.php: " . $e->getMessage());
    sendResponse(false, 'Database error occurred', null, 500);
} catch (Exception $e) {
    error_log("General error in getUserEmergencyContacts.php: " . $e->getMessage());
    sendResponse(false, 'An error occurred while retrieving emergency contacts', null, 500);
}
?>

