<?php
/**
 * Get Landmarks Endpoint
 * GET /landmarks
 * Fetches only active landmarks for mobile consumption
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../includes/Response.php';
require_once __DIR__ . '/../../includes/Validator.php';

try {
    // Enable error reporting for debugging
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
    
    // Try to initialize database, fallback to sample data if it fails
    try {
        $db = new Database();
        $conn = $db->getConnection();
    } catch (Exception $dbError) {
        // If database connection fails, use fallback data
        require_once __DIR__ . '/get_landmarks_fallback.php';
        exit();
    }
    
    // Get query parameters
    $page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
    $limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 50;
    $search = isset($_GET['search']) ? Validator::sanitizeString($_GET['search']) : '';
    $category = isset($_GET['category']) ? Validator::sanitizeString($_GET['category']) : '';
    $lat = isset($_GET['lat']) ? floatval($_GET['lat']) : null;
    $lng = isset($_GET['lng']) ? floatval($_GET['lng']) : null;
    $radius = isset($_GET['radius']) ? floatval($_GET['radius']) : null; // in kilometers
    
    $offset = ($page - 1) * $limit;
    
    // Build query - only fetch active landmarks
    $whereConditions = ["status = 'active'"];
    $params = [];
    
    // Add search filter
    if (!empty($search)) {
        $whereConditions[] = "(name LIKE ? OR description LIKE ? OR address LIKE ?)";
        $searchParam = "%$search%";
        $params = array_merge($params, [$searchParam, $searchParam, $searchParam]);
    }
    
    // Add category filter
    if (!empty($category)) {
        $whereConditions[] = "category = ?";
        $params[] = $category;
    }
    
    // Add location-based filtering if coordinates provided
    if ($lat !== null && $lng !== null && $radius !== null) {
        // For JSON coordinates, we'll need to extract lat/lng from JSON
        // This is a simplified approach - for production, consider using MySQL JSON functions
        $whereConditions[] = "JSON_EXTRACT(coordinates, '$.latitude') IS NOT NULL 
                             AND JSON_EXTRACT(coordinates, '$.longitude') IS NOT NULL
                             AND (
                                6371 * acos(
                                    cos(radians(?)) * cos(radians(JSON_EXTRACT(coordinates, '$.latitude'))) * 
                                    cos(radians(JSON_EXTRACT(coordinates, '$.longitude')) - radians(?)) + 
                                    sin(radians(?)) * sin(radians(JSON_EXTRACT(coordinates, '$.latitude')))
                                )
                             ) <= ?";
        $params = array_merge($params, [$lat, $lng, $lat, $radius]);
    }
    
    $whereClause = implode(' AND ', $whereConditions);
    
    // Get total count
    $countStmt = $conn->prepare("SELECT COUNT(*) as total FROM landmarks_pins WHERE $whereClause");
    $countStmt->execute($params);
    $total = $countStmt->fetch()['total'];
    
    // Get landmarks with mobile-optimized fields
    $landmarksStmt = $conn->prepare("
        SELECT 
            id,
            name,
            description,
            category,
            coordinates,
            address,
            phone,
            pin_color,
            opening_time,
            closing_time,
            is_open,
            created_at,
            updated_at
        FROM landmarks_pins 
        WHERE $whereClause
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?
    ");
    
    $params[] = $limit;
    $params[] = $offset;
    $landmarksStmt->execute($params);
    $landmarks = $landmarksStmt->fetchAll();
    
    // Get current Philippines time
    $philippinesTime = new DateTime('now', new DateTimeZone('Asia/Manila'));
    $currentTime = $philippinesTime->format('H:i:s');
    $currentHour = (int) $philippinesTime->format('H');
    $currentMinute = (int) $philippinesTime->format('i');
    $currentTimeMinutes = $currentHour * 60 + $currentMinute;
    
    // Process landmarks for mobile consumption
    foreach ($landmarks as &$landmark) {
        // Parse coordinates JSON if it's a string
        if (is_string($landmark['coordinates'])) {
            $coordinates = json_decode($landmark['coordinates'], true);
            if ($coordinates) {
                $landmark['coordinates'] = [
                    'latitude' => floatval($coordinates['latitude']),
                    'longitude' => floatval($coordinates['longitude'])
                ];
            }
        } elseif (is_array($landmark['coordinates'])) {
            // Already an array, ensure proper float values
            $landmark['coordinates'] = [
                'latitude' => floatval($landmark['coordinates']['latitude']),
                'longitude' => floatval($landmark['coordinates']['longitude'])
            ];
        }
        
        // Calculate if landmark is currently open based on Philippines time
        $isCurrentlyOpen = false;
        if (!empty($landmark['opening_time']) && !empty($landmark['closing_time'])) {
            // Parse opening and closing times
            $openingTime = $landmark['opening_time'];
            $closingTime = $landmark['closing_time'];
            
            // Convert times to minutes for easier comparison
            $openingParts = explode(':', $openingTime);
            $closingParts = explode(':', $closingTime);
            
            $openingMinutes = (int)$openingParts[0] * 60 + (int)$openingParts[1];
            $closingMinutes = (int)$closingParts[0] * 60 + (int)$closingParts[1];
            
            // Handle cases where closing time is next day (e.g., 22:00 to 06:00)
            if ($closingMinutes < $openingMinutes) {
                // Closing time is next day
                $isCurrentlyOpen = ($currentTimeMinutes >= $openingMinutes) || ($currentTimeMinutes < $closingMinutes);
            } else {
                // Same day operation
                $isCurrentlyOpen = ($currentTimeMinutes >= $openingMinutes) && ($currentTimeMinutes < $closingMinutes);
            }
        }
        
        // Update the landmark's is_open status in database if it has changed
        if ($isCurrentlyOpen != (bool)$landmark['is_open']) {
            try {
                $updateStmt = $conn->prepare("UPDATE landmarks_pins SET is_open = ? WHERE id = ?");
                $updateStmt->execute([$isCurrentlyOpen ? 1 : 0, $landmark['id']]);
            } catch (Exception $e) {
                // Log error but don't fail the request
                error_log("Failed to update landmark status for ID {$landmark['id']}: " . $e->getMessage());
            }
        }
        
        // Set the calculated status for the response
        $landmark['is_open'] = $isCurrentlyOpen;
        
        // Add time information for debugging
        $landmark['time_info'] = [
            'current_philippines_time' => $philippinesTime->format('Y-m-d H:i:s T'),
            'opening_time' => $landmark['opening_time'],
            'closing_time' => $landmark['closing_time'],
            'calculated_status' => $isCurrentlyOpen ? 'OPEN' : 'CLOSED'
        ];
        
        // Format timestamps
        $landmark['created_at_formatted'] = date('M j, Y g:i A', strtotime($landmark['created_at']));
        $landmark['updated_at_formatted'] = date('M j, Y g:i A', strtotime($landmark['updated_at']));
    }
    
    // Calculate pagination info
    $totalPages = ceil($total / $limit);
    
    // Get available categories for filtering
    $categoriesStmt = $conn->prepare("
        SELECT DISTINCT category 
        FROM landmarks_pins 
        WHERE status = 'active' 
        ORDER BY category
    ");
    $categoriesStmt->execute();
    $categories = $categoriesStmt->fetchAll(PDO::FETCH_COLUMN);
    
    Response::success([
        'landmarks' => $landmarks,
        'pagination' => [
            'current_page' => $page,
            'total_pages' => $totalPages,
            'total_items' => $total,
            'items_per_page' => $limit,
            'has_next' => $page < $totalPages,
            'has_prev' => $page > 1
        ],
        'filters' => [
            'categories' => $categories
        ],
        'api_info' => [
            'version' => '1.0.0',
            'endpoint' => 'landmarks',
            'description' => 'Mobile API for landmarks - active landmarks only'
        ]
    ], 'Landmarks retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to retrieve landmarks: ' . $e->getMessage());
}
?>
