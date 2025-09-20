<?php
/**
 * Get Users Endpoint
 * GET /users
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/SessionManager.php';

try {
    // Require authentication
    SessionManager::requireAuth();
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Get query parameters
    $page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
    $limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 20;
    $search = isset($_GET['search']) ? Validator::sanitizeString($_GET['search']) : '';
    $role = isset($_GET['role']) ? Validator::sanitizeString($_GET['role']) : '';
    $status = isset($_GET['status']) ? Validator::sanitizeString($_GET['status']) : '';
    
    $offset = ($page - 1) * $limit;
    
    // Build query
    $whereConditions = ['deleted_at IS NULL'];
    $params = [];
    
    if (!empty($search)) {
        $whereConditions[] = "(first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR username LIKE ?)";
        $searchParam = "%$search%";
        $params = array_merge($params, [$searchParam, $searchParam, $searchParam, $searchParam]);
    }
    
    if (!empty($role)) {
        $whereConditions[] = "role = ?";
        $params[] = $role;
    }
    
    if (!empty($status)) {
        $whereConditions[] = "status = ?";
        $params[] = $status;
    }
    
    $whereClause = implode(' AND ', $whereConditions);
    
    // Get total count
    $countStmt = $conn->prepare("SELECT COUNT(*) as total FROM users WHERE $whereClause");
    $countStmt->execute($params);
    $total = $countStmt->fetch()['total'];
    
    // Get users
    $usersStmt = $conn->prepare("
        SELECT id, first_name, last_name, email, username, role, status, phone_number, 
               last_login, created_at, updated_at
        FROM users 
        WHERE $whereClause
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?
    ");
    
    $params[] = $limit;
    $params[] = $offset;
    $usersStmt->execute($params);
    $users = $usersStmt->fetchAll();
    
    // Calculate pagination info
    $totalPages = ceil($total / $limit);
    
    Response::success([
        'users' => $users,
        'pagination' => [
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $total,
            'items_per_page' => $limit,
            'has_next' => $page < $totalPages,
            'has_prev' => $page > 1
        ]
    ], 'Users retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve users: ' . $e->getMessage());
}
?>

