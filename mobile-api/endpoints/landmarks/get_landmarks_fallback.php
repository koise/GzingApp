<?php
/**
 * Landmarks Fallback Data
 * Used when database connection fails
 */

// Sample landmarks data for testing
$sampleLandmarks = [
    [
        'id' => 1,
        'name' => 'LRT Station',
        'description' => 'A Railway to Recto Station',
        'category' => 'transport',
        'coordinates' => [
            'latitude' => 14.62490919,
            'longitude' => 121.12071524
        ],
        'address' => 'Marcos Highway Mayamot, Antipolo City, Rizal, Philippines',
        'phone' => '',
        'pin_color' => '#3b82f6',
        'opening_time' => '05:00:00',
        'closing_time' => '21:00:00',
        'is_open' => true,
        'created_at' => '2025-09-14 14:10:45',
        'updated_at' => '2025-09-14 14:10:45',
        'created_at_formatted' => 'Sep 14, 2025 2:10 PM',
        'updated_at_formatted' => 'Sep 14, 2025 2:10 PM'
    ],
    [
        'id' => 2,
        'name' => 'Blue Mountain',
        'description' => 'Blue Mountain in Antipolo City, Philippines, is a residential area that combines the best of business and residential living.',
        'category' => 'residential',
        'coordinates' => [
            'latitude' => 14.62398828,
            'longitude' => 121.15253904
        ],
        'address' => 'Marcos Highway Cupang, Antipolo City, Rizal, Philippines',
        'phone' => '',
        'pin_color' => '#93b1e1',
        'opening_time' => '08:00:00',
        'closing_time' => '22:00:00',
        'is_open' => true,
        'created_at' => '2025-09-14 14:06:32',
        'updated_at' => '2025-09-14 14:06:32',
        'created_at_formatted' => 'Sep 14, 2025 2:06 PM',
        'updated_at_formatted' => 'Sep 14, 2025 2:06 PM'
    ],
    [
        'id' => 3,
        'name' => 'Cafe Loupe',
        'description' => 'A cozy cafe in Antipolo City',
        'category' => 'business',
        'coordinates' => [
            'latitude' => 14.61770854,
            'longitude' => 121.14032806
        ],
        'address' => '267 Sumulong Highway Mayamot, Antipolo City, Rizal, Philippines',
        'phone' => '+63 917 123 4567',
        'pin_color' => '#193b71',
        'opening_time' => '10:00:00',
        'closing_time' => '22:00:00',
        'is_open' => true,
        'created_at' => '2025-09-16 07:35:30',
        'updated_at' => '2025-09-16 07:35:30',
        'created_at_formatted' => 'Sep 16, 2025 7:35 AM',
        'updated_at_formatted' => 'Sep 16, 2025 7:35 AM'
    ],
    [
        'id' => 4,
        'name' => 'Our Lady of Fatima',
        'description' => 'A Catholic school in Antipolo City',
        'category' => 'school',
        'coordinates' => [
            'latitude' => 14.61934259,
            'longitude' => 121.1524571
        ],
        'address' => '120 Macarthur Highway Cupang, Antipolo City, Rizal, Philippines',
        'phone' => '+63 2 123 4567',
        'pin_color' => '#dd370e',
        'opening_time' => '08:00:00',
        'closing_time' => '22:00:00',
        'is_open' => true,
        'created_at' => '2025-09-16 07:30:57',
        'updated_at' => '2025-09-16 07:31:05',
        'created_at_formatted' => 'Sep 16, 2025 7:30 AM',
        'updated_at_formatted' => 'Sep 16, 2025 7:31 AM'
    ],
    [
        'id' => 5,
        'name' => 'Cloud 9',
        'description' => 'A recreational facility in Antipolo City',
        'category' => 'recreation',
        'coordinates' => [
            'latitude' => 14.61360506,
            'longitude' => 121.15428515
        ],
        'address' => 'Sumulong Highway Cupang, Antipolo City, Rizal, Philippines',
        'phone' => '+63 917 765 4321',
        'pin_color' => '#3b82f6',
        'opening_time' => '08:00:00',
        'closing_time' => '22:00:00',
        'is_open' => true,
        'created_at' => '2025-09-16 07:24:22',
        'updated_at' => '2025-09-16 07:24:22',
        'created_at_formatted' => 'Sep 16, 2025 7:24 AM',
        'updated_at_formatted' => 'Sep 16, 2025 7:24 AM'
    ]
];

// Apply search filter if provided
$search = isset($_GET['search']) ? Validator::sanitizeString($_GET['search']) : '';
if (!empty($search)) {
    $sampleLandmarks = array_filter($sampleLandmarks, function($landmark) use ($search) {
        return stripos($landmark['name'], $search) !== false || 
               stripos($landmark['description'], $search) !== false ||
               stripos($landmark['address'], $search) !== false;
    });
}

// Apply category filter if provided
$category = isset($_GET['category']) ? Validator::sanitizeString($_GET['category']) : '';
if (!empty($category)) {
    $sampleLandmarks = array_filter($sampleLandmarks, function($landmark) use ($category) {
        return $landmark['category'] === $category;
    });
}

// Get pagination parameters
$page = isset($_GET['page']) ? max(1, intval($_GET['page'])) : 1;
$limit = isset($_GET['limit']) ? min(100, max(1, intval($_GET['limit']))) : 50;

$total = count($sampleLandmarks);
$offset = ($page - 1) * $limit;
$landmarks = array_slice($sampleLandmarks, $offset, $limit);

// Calculate pagination info
$totalPages = ceil($total / $limit);

// Available categories
$categories = ['transport', 'residential', 'business', 'school', 'recreation'];

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
        'description' => 'Mobile API for landmarks - active landmarks only (FALLBACK DATA)',
        'note' => 'Using fallback data due to database connection issues'
    ]
], 'Landmarks retrieved successfully (fallback data)');
?>
