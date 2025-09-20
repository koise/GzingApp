<?php
/**
 * Direct Create SOS Contact Endpoint
 * POST /mobile-api/sos/create_contact.php
 */

// Enable CORS for mobile app
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
header('Access-Control-Allow-Credentials: true');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Set content type
header('Content-Type: application/json');

// Include required files
require_once '../config/database.php';
require_once '../includes/Response.php';
require_once '../includes/Validator.php';

try {
    // Only allow POST requests
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        Response::methodNotAllowed();
    }
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input');
    }
    
    // Validate required fields
    $requiredFields = ['user_id', 'name', 'phone_number', 'relationship'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $userId = intval($input['user_id']);
    $name = Validator::sanitizeString($input['name']);
    $phoneNumber = Validator::sanitizePhone($input['phone_number']);
    $relationship = Validator::sanitizeString($input['relationship']);
    $isPrimary = isset($input['is_primary']) ? filter_var($input['is_primary'], FILTER_VALIDATE_BOOLEAN) : false;
    
    // Validate phone number
    if (!Validator::validatePhone($phoneNumber)) {
        Response::error('Invalid phone number format. Use +639XXXXXXXXX or 09XXXXXXXXX');
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // If setting as primary, unset other primary contacts
    if ($isPrimary) {
        $unsetPrimaryStmt = $conn->prepare("
            UPDATE sos_contacts 
            SET is_primary = 0 
            WHERE user_id = ? AND deleted_at IS NULL
        ");
        $unsetPrimaryStmt->execute([$userId]);
    }
    
    // Insert new contact
    $insertStmt = $conn->prepare("
        INSERT INTO sos_contacts (user_id, name, phone_number, relationship, is_primary, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, NOW(), NOW())
    ");
    
    $insertStmt->execute([
        $userId,
        $name,
        $phoneNumber,
        $relationship,
        $isPrimary ? 1 : 0
    ]);
    
    $contactId = $conn->lastInsertId();
    
    // Get the created contact data
    $contactStmt = $conn->prepare("
        SELECT id, user_id, name, phone_number, relationship, is_primary, created_at, updated_at
        FROM sos_contacts WHERE id = ?
    ");
    $contactStmt->execute([$contactId]);
    $contact = $contactStmt->fetch();
    
    // Return success response
    Response::success([
        'contact' => $contact
    ], 'SOS contact created successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to create SOS contact: ' . $e->getMessage());
}
?>

