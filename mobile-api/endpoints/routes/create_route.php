<?php
/**
 * Create Route Endpoint
 * POST /routes
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';
require_once '../../includes/Validator.php';
require_once '../../includes/SessionManager.php';

try {
    // Require authentication
    SessionManager::requireAuth();
    
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        Response::error('Invalid JSON input');
    }
    
    // Validate required fields
    $requiredFields = ['name'];
    $errors = Validator::validateRequired($requiredFields, $input);
    
    if (!empty($errors)) {
        Response::validationError($errors);
    }
    
    // Sanitize input
    $name = Validator::sanitizeString($input['name']);
    $description = isset($input['description']) ? Validator::sanitizeString($input['description']) : null;
    $pincount = isset($input['pincount']) ? max(0, intval($input['pincount'])) : 0;
    $kilometer = isset($input['kilometer']) ? max(0, floatval($input['kilometer'])) : 0.00;
    $estimatedTotalFare = isset($input['estimated_total_fare']) ? max(0, floatval($input['estimated_total_fare'])) : 0.00;
    $mapDetails = isset($input['map_details']) ? $input['map_details'] : null;
    $status = isset($input['status']) ? Validator::sanitizeString($input['status']) : 'active';
    
    // Validate status
    if (!in_array($status, ['active', 'inactive', 'maintenance'])) {
        Response::error('Invalid status. Must be active, inactive, or maintenance');
    }
    
    // Validate map_details if provided
    if ($mapDetails && !Validator::validateJson(json_encode($mapDetails))) {
        Response::error('Invalid map_details format');
    }
    
    // Initialize database
    $db = new Database();
    $conn = $db->getConnection();
    
    // Check if route name already exists
    $nameStmt = $conn->prepare("SELECT id FROM routes WHERE name = ?");
    $nameStmt->execute([$name]);
    if ($nameStmt->fetch()) {
        Response::error('Route name already exists');
    }
    
    // Insert new route
    $insertStmt = $conn->prepare("
        INSERT INTO routes (name, description, pincount, kilometer, estimated_total_fare, map_details, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
    ");
    
    $mapDetailsJson = $mapDetails ? json_encode($mapDetails) : null;
    
    $insertStmt->execute([
        $name,
        $description,
        $pincount,
        $kilometer,
        $estimatedTotalFare,
        $mapDetailsJson,
        $status
    ]);
    
    $routeId = $conn->lastInsertId();
    
    // Log the route creation
    $currentUser = SessionManager::getUserData();
    $logStmt = $conn->prepare("
        INSERT INTO user_activity_logs (log_type, log_level, user_name, action, message, user_agent, additional_data, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
    ");
    
    $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'Unknown';
    $logData = json_encode([
        'route_id' => $routeId,
        'created_by' => $currentUser['id'],
        'route_name' => $name,
        'pincount' => $pincount,
        'kilometer' => $kilometer,
        'status' => $status,
        'ip_address' => $_SERVER['REMOTE_ADDR'] ?? 'Unknown',
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
    $logStmt->execute([
        'route_management',
        'info',
        $currentUser['first_name'] . ' ' . $currentUser['last_name'],
        'Route Created',
        'Route ' . $name . ' created successfully',
        $userAgent,
        $logData
    ]);
    
    // Get the created route data
    $routeStmt = $conn->prepare("
        SELECT id, name, description, pincount, kilometer, estimated_total_fare, 
               map_details, status, created_at, updated_at
        FROM routes WHERE id = ?
    ");
    $routeStmt->execute([$routeId]);
    $route = $routeStmt->fetch();
    
    // Parse map_details JSON
    if ($route['map_details']) {
        $route['map_details'] = json_decode($route['map_details'], true);
    }
    
    // Return success response
    Response::success([
        'route' => $route
    ], 'Route created successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to create route: ' . $e->getMessage());
}
?>

