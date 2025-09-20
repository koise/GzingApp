<?php
/**
 * Get Routes Endpoint
 * GET /routes
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../includes/Response.php';
require_once __DIR__ . '/../../includes/SessionManager.php';
require_once __DIR__ . '/../../includes/Validator.php';

try {
    // Enable error reporting for debugging
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
    
    // Temporarily disable authentication for testing
    // TODO: Re-enable authentication later
    // SessionManager::requireAuth();
    
    // Try to initialize database, fallback to sample data if it fails
    try {
        $db = new Database();
        $conn = $db->getConnection();
    } catch (Exception $dbError) {
        // If database connection fails, use fallback data
        require_once __DIR__ . '/get_routes_fallback.php';
        exit();
    }
    
    // Get query parameters
    $page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
    $limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 20;
    $search = isset($_GET['search']) ? Validator::sanitizeString($_GET['search']) : '';
    $status = isset($_GET['status']) ? Validator::sanitizeString($_GET['status']) : 'active'; // Default to active routes only
    
    // Validate status parameter
    if (!in_array($status, ['active', 'inactive', 'maintenance'])) {
        Response::error('Invalid status. Must be active, inactive, or maintenance');
    }
    
    $offset = ($page - 1) * $limit;
    
    // Build query
    $whereConditions = [];
    $params = [];
    
    // Always filter by status (default to active)
    $whereConditions[] = "status = ?";
    $params[] = $status;
    
    if (!empty($search)) {
        $whereConditions[] = "(name LIKE ? OR description LIKE ?)";
        $searchParam = "%$search%";
        $params = array_merge($params, [$searchParam, $searchParam]);
    }
    
    $whereClause = empty($whereConditions) ? '1=1' : implode(' AND ', $whereConditions);
    
    // Get total count
    $countStmt = $conn->prepare("SELECT COUNT(*) as total FROM routes WHERE $whereClause");
    $countStmt->execute($params);
    $total = $countStmt->fetch()['total'];
    
    // Get routes
    $routesStmt = $conn->prepare("
        SELECT id, name, description, pincount, kilometer, estimated_total_fare, 
               map_details, status, created_at, updated_at
        FROM routes 
        WHERE $whereClause
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?
    ");
    
    $params[] = $limit;
    $params[] = $offset;
    $routesStmt->execute($params);
    $routes = $routesStmt->fetchAll();
    
    // Parse map_details JSON for each route
    foreach ($routes as &$route) {
        if ($route['map_details']) {
            $route['map_details'] = json_decode($route['map_details'], true);
        }
    }
    
    // Calculate pagination info
    $totalPages = ceil($total / $limit);
    
    Response::success([
        'routes' => $routes,
        'pagination' => [
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $total,
            'items_per_page' => $limit,
            'has_next' => $page < $totalPages,
            'has_prev' => $page > 1
        ]
    ], 'Routes retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve routes: ' . $e->getMessage());
}
?>
