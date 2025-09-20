    <?php
// Simple endpoint to fetch a single route by id, similar to get_routes.php response structure
header('Content-Type: application/json');

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../includes/Response.php';

try {
    // Enable error reporting for debugging
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
    
    $id = isset($_GET['id']) ? intval($_GET['id']) : 0;
    if ($id <= 0) {
        echo json_encode([ 'success' => false, 'message' => 'Missing or invalid id' ]);
        exit;
    }

    // Initialize database
    $db = new Database();
    $pdo = $db->getConnection();
    
    // Debug: Log the ID being searched
    error_log("Searching for route ID: " . $id);
    
    // Fetch route core data
    $stmt = $pdo->prepare('SELECT * FROM routes WHERE id = :id LIMIT 1');
    $stmt->execute([':id' => $id]);
    $route = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$route) {
        error_log("Route not found for ID: " . $id);
        echo json_encode([ 'success' => false, 'message' => 'Route not found' ]);
        exit;
    }
    
    error_log("Route found: " . $route['name']);

    // Parse map_details JSON to extract pins and route line
    $mapDetails = null;
    $pins = [];
    $routeLine = null;
    
    if (!empty($route['map_details'])) {
        $mapDetails = json_decode($route['map_details'], true);
        if ($mapDetails && isset($mapDetails['pins'])) {
            $pins = $mapDetails['pins'];
        }
        if ($mapDetails && isset($mapDetails['routeLine'])) {
            $routeLine = $mapDetails['routeLine'];
        }
    }

    $data = [
        'route' => [
            'id' => intval($route['id']),
            'name' => $route['name'] ?? '',
            'kilometer' => floatval($route['kilometer'] ?? 0),
            'pinCount' => count($pins),
            'estimatedTotalFare' => floatval($route['estimated_total_fare'] ?? 0),
            'mapDetails' => [
                'pins' => $pins,
                'routeLine' => $routeLine
            ]
        ]
    ];

    echo json_encode([ 'success' => true, 'message' => 'OK', 'data' => $data ]);
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode([ 'success' => false, 'message' => 'Server error', 'error' => $e->getMessage() ]);
}


    