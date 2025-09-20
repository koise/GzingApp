<?php
/**
 * Get SOS Contacts Endpoint
 * GET /sos-contacts
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/SessionManager.php';

try {
    // Require authentication
    SessionManager::requireAuth();
    
    $currentUser = SessionManager::getUserData();
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get query parameters
    $page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
    $limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 20;
    $search = isset($_GET['search']) ? Validator::sanitizeString($_GET['search']) : '';
    $relationship = isset($_GET['relationship']) ? Validator::sanitizeString($_GET['relationship']) : '';
    $isPrimary = isset($_GET['is_primary']) ? filter_var($_GET['is_primary'], FILTER_VALIDATE_BOOLEAN) : null;
    
    $offset = ($page - 1) * $limit;
    
    // Build query - users can only see their own contacts
    $whereConditions = ['user_id = ?', 'deleted_at IS NULL'];
    $params = [$currentUser['id']];
    
    if (!empty($search)) {
        $whereConditions[] = "(name LIKE ? OR phone_number LIKE ?)";
        $searchParam = "%$search%";
        $params = array_merge($params, [$searchParam, $searchParam]);
    }
    
    if (!empty($relationship)) {
        $whereConditions[] = "relationship = ?";
        $params[] = $relationship;
    }
    
    if ($isPrimary !== null) {
        $whereConditions[] = "is_primary = ?";
        $params[] = $isPrimary ? 1 : 0;
    }
    
    $whereClause = implode(' AND ', $whereConditions);
    
    // Get total count
    $countStmt = $conn->prepare("SELECT COUNT(*) as total FROM sos_contacts WHERE $whereClause");
    $countStmt->execute($params);
    $total = $countStmt->fetch()['total'];
    
    // Get contacts
    $contactsStmt = $conn->prepare("
        SELECT id, user_id, name, phone_number, relationship, is_primary, created_at, updated_at
        FROM sos_contacts 
        WHERE $whereClause
        ORDER BY is_primary DESC, created_at DESC
        LIMIT ? OFFSET ?
    ");
    
    $params[] = $limit;
    $params[] = $offset;
    $contactsStmt->execute($params);
    $contacts = $contactsStmt->fetchAll();
    
    // Calculate pagination info
    $totalPages = ceil($total / $limit);
    
    Response::success([
        'contacts' => $contacts,
        'pagination' => [
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $total,
            'items_per_page' => $limit,
            'has_next' => $page < $totalPages,
            'has_prev' => $page > 1
        ]
    ], 'SOS contacts retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve SOS contacts: ' . $e->getMessage());
}
?>

