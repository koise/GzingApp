<?php
/**
 * Create SOS Contact Endpoint
 * POST /sos-contacts
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/Validator.php';
require_once '../../includes/SessionManager.php';

try {
    // Require authentication
    SessionManager::requireAuth();
    
    $currentUser = SessionManager::getUserData();
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input');
    }
    
    // Validate required fields
    $requiredFields = ['name', 'phone_number', 'relationship'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
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
        $unsetPrimaryStmt->execute([$currentUser['id']]);
    }
    
    // Insert new contact
    $insertStmt = $conn->prepare("
        INSERT INTO sos_contacts (user_id, name, phone_number, relationship, is_primary, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, NOW(), NOW())
    ");
    
    $insertStmt->execute([
        $currentUser['id'],
        $name,
        $phoneNumber,
        $relationship,
        $isPrimary ? 1 : 0
    ]);
    
    $contactId = $conn->lastInsertId();
    
    // Log the contact creation
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'contact_id' => $contactId,
        'user_id' => $currentUser['id'],
        'contact_name' => $name,
        'phone_number' => $phoneNumber,
        'relationship' => $relationship,
        'is_primary' => $isPrimary,
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'sos_contact_create',
        'info',
        $currentUser['first_name'] . ' ' . $currentUser['last_name'],
        'SOS Contact Created',
        'SOS contact ' . $name . ' created',
        $userAgent,
        $logData
    ]);
    
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

