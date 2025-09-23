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

// Suppress all errors to prevent HTML output
error_reporting(0);
ini_set('display_errors', 0);

// Set error handlers to return JSON only
set_error_handler(function($severity, $message, $file, $line) {
    error_log("PHP Error: $message in $file on line $line");
    return true;
});

set_exception_handler(function($exception) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Server error: ' . $exception->getMessage()
    ]);
    exit();
});

// Database configuration with fallback hosts
$hosts = ['localhost'];
$dbname = 'u126959096_gzing_admin';
$username = 'u126959096_gzing_admin';
$password = 'X6v8M$U9;j';

$pdo = null;
foreach ($hosts as $host) {
    try {
        $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        break;
    } catch (PDOException $e) {
        continue;
    }
}

if (!$pdo) {
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
if (!isset($input['contact_id']) || empty($input['contact_id'])) {
    sendResponse(false, 'contact_id is required', null, 400);
}

$contactId = (int)$input['contact_id'];
$name = isset($input['name']) ? trim($input['name']) : null;
$phoneNumber = isset($input['phone_number']) ? trim($input['phone_number']) : null;
$relationship = isset($input['relationship']) ? trim($input['relationship']) : null;
$isPrimary = isset($input['is_primary']) ? (bool)$input['is_primary'] : null;

// Check if at least one field is provided for update
if ($name === null && $phoneNumber === null && $relationship === null && $isPrimary === null) {
    sendResponse(false, 'At least one field (name, phone_number, relationship, is_primary) must be provided for update', null, 400);
}

try {
    // Start transaction
    $pdo->beginTransaction();
    
    // First, get the existing contact and user information
    $existingQuery = "SELECT sc.*, u.phone_number as user_phone_number 
                      FROM sos_contacts sc
                      JOIN users u ON sc.user_id = u.id
                      WHERE sc.id = ? AND sc.deleted_at IS NULL AND u.deleted_at IS NULL";
    $existingStmt = $pdo->prepare($existingQuery);
    $existingStmt->execute([$contactId]);
    $existingContact = $existingStmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$existingContact) {
        $pdo->rollBack();
        sendResponse(false, 'Emergency contact not found', null, 404);
    }
    
    $userId = $existingContact['user_id'];
    
    // Validate phone number if provided
    if ($phoneNumber !== null) {
        if (!preg_match('/^\+?[0-9]{10,15}$/', $phoneNumber)) {
            $pdo->rollBack();
            sendResponse(false, 'Invalid phone number format', null, 400);
        }
        
        // Check if the new phone number is the same as user's own number
        if ($existingContact['user_phone_number'] && $existingContact['user_phone_number'] === $phoneNumber) {
            $pdo->rollBack();
            sendResponse(false, 'Cannot use your own phone number as an emergency contact', null, 400);
        }
        
        // Check if another contact with the same phone number already exists for this user
        if ($phoneNumber !== $existingContact['phone_number']) {
            $duplicateQuery = "SELECT id FROM sos_contacts WHERE user_id = ? AND phone_number = ? AND id != ? AND deleted_at IS NULL";
            $duplicateStmt = $pdo->prepare($duplicateQuery);
            $duplicateStmt->execute([$userId, $phoneNumber, $contactId]);
            
            if ($duplicateStmt->fetch()) {
                $pdo->rollBack();
                sendResponse(false, 'Another emergency contact with this phone number already exists', null, 400);
            }
        }
    }
    
    // Validate relationship if provided
    if ($relationship !== null) {
        $validRelationships = ['Family', 'Friend', 'Partner', 'Others'];
        if (!in_array($relationship, $validRelationships)) {
            $pdo->rollBack();
            sendResponse(false, 'Invalid relationship. Must be one of: ' . implode(', ', $validRelationships), null, 400);
        }
    }
    
    // If setting as primary, unset other primary contacts for this user
    if ($isPrimary === true) {
        $unsetPrimaryQuery = "UPDATE sos_contacts SET is_primary = 0 WHERE user_id = ? AND id != ? AND deleted_at IS NULL";
        $unsetPrimaryStmt = $pdo->prepare($unsetPrimaryQuery);
        $unsetPrimaryStmt->execute([$userId, $contactId]);
    }
    
    // Build update query dynamically
    $updateFields = [];
    $updateValues = [];
    
    if ($name !== null) {
        $updateFields[] = "name = ?";
        $updateValues[] = $name;
    }
    
    if ($phoneNumber !== null) {
        $updateFields[] = "phone_number = ?";
        $updateValues[] = $phoneNumber;
    }
    
    if ($relationship !== null) {
        $updateFields[] = "relationship = ?";
        $updateValues[] = $relationship;
    }
    
    if ($isPrimary !== null) {
        $updateFields[] = "is_primary = ?";
        $updateValues[] = $isPrimary ? 1 : 0;
    }
    
    $updateFields[] = "updated_at = NOW()";
    $updateValues[] = $contactId;
    
    $updateQuery = "UPDATE sos_contacts SET " . implode(', ', $updateFields) . " WHERE id = ?";
    $updateStmt = $pdo->prepare($updateQuery);
    $updateStmt->execute($updateValues);
    
    // Get the updated contact details
    $selectQuery = "SELECT id, user_id, name, phone_number, relationship, is_primary, created_at, updated_at 
                    FROM sos_contacts WHERE id = ?";
    $selectStmt = $pdo->prepare($selectQuery);
    $selectStmt->execute([$contactId]);
    $updatedContact = $selectStmt->fetch(PDO::FETCH_ASSOC);
    
    // Commit transaction
    $pdo->commit();
    
    // Log the activity
    $logQuery = "INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at) 
                 VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
    $logStmt = $pdo->prepare($logQuery);
    $logStmt->execute([
        'sos_contact_management',
        'info',
        $updatedContact['name'],
        'Emergency Contact Updated',
        "Emergency contact '{$updatedContact['name']}' updated successfully for user ID $userId",
        $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown',
        json_encode([
            'user_id' => $userId,
            'contact_id' => $contactId,
            'contact_name' => $updatedContact['name'],
            'phone_number' => $updatedContact['phone_number'],
            'relationship' => $updatedContact['relationship'],
            'is_primary' => (bool)$updatedContact['is_primary'],
            'action' => 'update_emergency_contact',
            'updated_fields' => array_keys(array_filter([
                'name' => $name,
                'phone_number' => $phoneNumber,
                'relationship' => $relationship,
                'is_primary' => $isPrimary
            ], function($value) { return $value !== null; }))
        ])
    ]);
    
    sendResponse(true, 'Emergency contact updated successfully', [
        'contact' => $updatedContact
    ]);
    
} catch (PDOException $e) {
    $pdo->rollBack();
    error_log("Database error in updateEmergencyContact.php: " . $e->getMessage());
    sendResponse(false, 'Database error occurred', null, 500);
} catch (Exception $e) {
    $pdo->rollBack();
    error_log("General error in updateEmergencyContact.php: " . $e->getMessage());
    sendResponse(false, 'An error occurred while updating the emergency contact', null, 500);
}
?>

