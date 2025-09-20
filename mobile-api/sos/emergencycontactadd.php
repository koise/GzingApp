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

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Only POST method is allowed', null, 405);
}

// Get JSON input
$input = json_decode(file_get_contents('php://input'), true);

// Validate required fields
$requiredFields = ['user_id', 'name', 'phone_number', 'relationship'];
foreach ($requiredFields as $field) {
    if (!isset($input[$field]) || empty(trim($input[$field]))) {
        sendResponse(false, "Missing required field: $field", null, 400);
    }
}

$userId = (int)$input['user_id'];
$name = trim($input['name']);
$phoneNumber = trim($input['phone_number']);
$relationship = trim($input['relationship']);
$isPrimary = isset($input['is_primary']) ? (bool)$input['is_primary'] : false;

// Validate phone number format (basic validation)
if (!preg_match('/^\+?[0-9]{10,15}$/', $phoneNumber)) {
    sendResponse(false, 'Invalid phone number format', null, 400);
}

// Validate relationship
$validRelationships = ['Family', 'Friend', 'Partner', 'Others'];
if (!in_array($relationship, $validRelationships)) {
    sendResponse(false, 'Invalid relationship. Must be one of: ' . implode(', ', $validRelationships), null, 400);
}

try {
    // Start transaction
    $pdo->beginTransaction();
    
    // First, get the user's own phone number to check for duplicates
    $userQuery = "SELECT phone_number FROM users WHERE id = ? AND deleted_at IS NULL";
    $userStmt = $pdo->prepare($userQuery);
    $userStmt->execute([$userId]);
    $user = $userStmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$user) {
        $pdo->rollBack();
        sendResponse(false, 'User not found', null, 404);
    }
    
    // Check if user already has 3 emergency contacts
    $countQuery = "SELECT COUNT(*) as contact_count FROM sos_contacts WHERE user_id = ? AND deleted_at IS NULL";
    $countStmt = $pdo->prepare($countQuery);
    $countStmt->execute([$userId]);
    $countResult = $countStmt->fetch(PDO::FETCH_ASSOC);
    
    if ($countResult['contact_count'] >= 3) {
        $pdo->rollBack();
        sendResponse(false, 'Maximum of 3 emergency contacts allowed. Please delete an existing contact before adding a new one.', null, 400);
    }
    
    // Check if the contact's phone number is the same as the user's phone number
    if ($user['phone_number'] && $user['phone_number'] === $phoneNumber) {
        $pdo->rollBack();
        sendResponse(false, 'Cannot add your own phone number as an emergency contact', null, 400);
    }
    
    // Check if contact with same phone number already exists for this user
    $duplicateQuery = "SELECT id FROM sos_contacts WHERE user_id = ? AND phone_number = ? AND deleted_at IS NULL";
    $duplicateStmt = $pdo->prepare($duplicateQuery);
    $duplicateStmt->execute([$userId, $phoneNumber]);
    
    if ($duplicateStmt->fetch()) {
        $pdo->rollBack();
        sendResponse(false, 'Emergency contact with this phone number already exists', null, 400);
    }
    
    // If this is being set as primary, unset other primary contacts for this user
    if ($isPrimary) {
        $unsetPrimaryQuery = "UPDATE sos_contacts SET is_primary = 0 WHERE user_id = ? AND deleted_at IS NULL";
        $unsetPrimaryStmt = $pdo->prepare($unsetPrimaryQuery);
        $unsetPrimaryStmt->execute([$userId]);
    }
    
    // Insert the new emergency contact
    $insertQuery = "INSERT INTO sos_contacts (user_id, name, phone_number, relationship, is_primary, created_at, updated_at) 
                    VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
    $insertStmt = $pdo->prepare($insertQuery);
    $insertStmt->execute([$userId, $name, $phoneNumber, $relationship, $isPrimary ? 1 : 0]);
    
    $contactId = $pdo->lastInsertId();
    
    // Get the created contact details
    $selectQuery = "SELECT id, user_id, name, phone_number, relationship, is_primary, created_at, updated_at 
                    FROM sos_contacts WHERE id = ?";
    $selectStmt = $pdo->prepare($selectQuery);
    $selectStmt->execute([$contactId]);
    $contact = $selectStmt->fetch(PDO::FETCH_ASSOC);
    
    // Commit transaction
    $pdo->commit();
    
    // Log the activity
    $logQuery = "INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at) 
                 VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
    $logStmt = $pdo->prepare($logQuery);
    $logStmt->execute([
        'sos_contact_management',
        'info',
        $name, // Using contact name as user_name for logging
        'Emergency Contact Added',
        "Emergency contact '$name' added successfully for user ID $userId",
        $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown',
        json_encode([
            'user_id' => $userId,
            'contact_id' => $contactId,
            'contact_name' => $name,
            'phone_number' => $phoneNumber,
            'relationship' => $relationship,
            'is_primary' => $isPrimary,
            'action' => 'add_emergency_contact'
        ])
    ]);
    
    sendResponse(true, 'Emergency contact added successfully', [
        'contact' => $contact
    ]);
    
} catch (PDOException $e) {
    $pdo->rollBack();
    error_log("Database error in emergencycontactadd.php: " . $e->getMessage());
    sendResponse(false, 'Database error occurred', null, 500);
} catch (Exception $e) {
    $pdo->rollBack();
    error_log("General error in emergencycontactadd.php: " . $e->getMessage());
    sendResponse(false, 'An error occurred while adding the emergency contact', null, 500);
}
?>
