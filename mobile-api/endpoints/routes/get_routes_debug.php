<?php
/**
 * Debug version of Get Routes Endpoint
 * GET /routes
 */

// Set error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set content type
header('Content-Type: application/json');

try {
    // Test basic functionality
    $page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
    $limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 20;
    $search = isset($_GET['search']) ? trim($_GET['search']) : '';
    $status = isset($_GET['status']) ? trim($_GET['status']) : 'active';
    
    // Validate status parameter
    if (!in_array($status, ['active', 'inactive', 'maintenance'])) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Invalid status. Must be active, inactive, or maintenance'
        ]);
        exit;
    }
    
    // Test database connection
    require_once __DIR__ . '/../../config/database.php';
    $db = new Database();
    $conn = $db->getConnection();
    
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
    
    $whereClause = implode(' AND ', $whereConditions);
    
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
    
    // Return success response
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Routes retrieved successfully',
        'data' => [
            'routes' => $routes,
            'pagination' => [
                'current_page' => $page,
                'total_pages' => $totalPages,
                'total_items' => $total,
                'items_per_page' => $limit,
                'has_next' => $page < $totalPages,
                'has_prev' => $page > 1
            ]
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
} catch (Exception $e) {
    // Return error response
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Failed to retrieve routes: ' . $e->getMessage(),
        'error_details' => [
            'file' => $e->getFile(),
            'line' => $e->getLine(),
            'trace' => $e->getTraceAsString()
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ]);
} catch (Error $e) {
    // Catch fatal errors
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Fatal error: ' . $e->getMessage(),
        'error_details' => [
            'file' => $e->getFile(),
            'line' => $e->getLine(),
            'trace' => $e->getTraceAsString()
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>
