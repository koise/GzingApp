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
if (!isset($input['contact_id']) || empty($input['contact_id'])) {
    sendResponse(false, 'contact_id is required', null, 400);
}

$contactId = (int)$input['contact_id'];
$permanentDelete = isset($input['permanent']) ? (bool)$input['permanent'] : false;

try {
    // Start transaction
    $pdo->beginTransaction();
    
    // First, get the existing contact information
    $existingQuery = "SELECT sc.*, u.first_name, u.last_name 
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
    $contactName = $existingContact['name'];
    $wasPrimary = $existingContact['is_primary'];
    
    if ($permanentDelete) {
        // Permanent delete - remove from database completely
        $deleteQuery = "DELETE FROM sos_contacts WHERE id = ?";
        $deleteStmt = $pdo->prepare($deleteQuery);
        $deleteStmt->execute([$contactId]);
        
        $action = 'Emergency Contact Permanently Deleted';
        $message = "Emergency contact '$contactName' permanently deleted for user ID $userId";
    } else {
        // Soft delete - set deleted_at timestamp
        $deleteQuery = "UPDATE sos_contacts SET deleted_at = NOW() WHERE id = ?";
        $deleteStmt = $pdo->prepare($deleteQuery);
        $deleteStmt->execute([$contactId]);
        
        $action = 'Emergency Contact Deleted';
        $message = "Emergency contact '$contactName' deleted for user ID $userId";
    }
    
    // If the deleted contact was primary, set another contact as primary (if any exist)
    if ($wasPrimary) {
        $setNewPrimaryQuery = "UPDATE sos_contacts 
                              SET is_primary = 1, updated_at = NOW() 
                              WHERE user_id = ? AND id != ? AND deleted_at IS NULL 
                              ORDER BY created_at ASC 
                              LIMIT 1";
        $setNewPrimaryStmt = $pdo->prepare($setNewPrimaryQuery);
        $setNewPrimaryStmt->execute([$userId, $contactId]);
        
        if ($setNewPrimaryStmt->rowCount() > 0) {
            $message .= " and a new primary contact was automatically set";
        }
    }
    
    // Commit transaction
    $pdo->commit();
    
    // Log the activity
    $logQuery = "INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at) 
                 VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
    $logStmt = $pdo->prepare($logQuery);
    $logStmt->execute([
        'sos_contact_management',
        'info',
        $contactName,
        $action,
        $message,
        $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown',
        json_encode([
            'user_id' => $userId,
            'contact_id' => $contactId,
            'contact_name' => $contactName,
            'phone_number' => $existingContact['phone_number'],
            'relationship' => $existingContact['relationship'],
            'was_primary' => (bool)$wasPrimary,
            'permanent_delete' => $permanentDelete,
            'action' => 'delete_emergency_contact'
        ])
    ]);
    
    // Get remaining contacts count for this user
    $countQuery = "SELECT COUNT(*) as remaining_count FROM sos_contacts WHERE user_id = ? AND deleted_at IS NULL";
    $countStmt = $pdo->prepare($countQuery);
    $countStmt->execute([$userId]);
    $remainingCount = $countStmt->fetch(PDO::FETCH_ASSOC)['remaining_count'];
    
    sendResponse(true, 'Emergency contact deleted successfully', [
        'deleted_contact' => [
            'id' => $contactId,
            'name' => $contactName,
            'was_primary' => (bool)$wasPrimary
        ],
        'remaining_contacts' => (int)$remainingCount,
        'permanent_delete' => $permanentDelete
    ]);
    
} catch (PDOException $e) {
    $pdo->rollBack();
    error_log("Database error in deleteEmergencyContact.php: " . $e->getMessage());
    sendResponse(false, 'Database error occurred', null, 500);
} catch (Exception $e) {
    $pdo->rollBack();
    error_log("General error in deleteEmergencyContact.php: " . $e->getMessage());
    sendResponse(false, 'An error occurred while deleting the emergency contact', null, 500);
}
?>

