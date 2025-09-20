<?php
/**
 * Emergency SMS Service API
 * Sends emergency SMS with current location and geocoding information
 * 
 * Endpoint: POST /mobile-api/endpoints/sms/send_emergency_sms
 * 
 * Required Parameters:
 * - user_id: User ID
 * - latitude: Current latitude
 * - longitude: Current longitude
 * - emergency_type: Type of emergency (optional)
 * - message: Custom message (optional)
 * - contacts: Array of phone numbers to send SMS to
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed. Only POST requests are accepted.'
    ]);
    exit();
}

// Include database configuration
@include_once __DIR__ . '/../../config/database.php';

// SMS Service Configuration
$SMS_API_TOKEN = 'dO1YvHSMoFyHSV8FKoYl6kAx0ndKO8ToaxhgdRDE';
$SMS_API_URL = 'https://api.philsms.com/send';

try {
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input) {
        throw new Exception('Invalid JSON input');
    }
    
    // Validate required parameters
    $required_fields = ['user_id', 'latitude', 'longitude', 'contacts'];
    foreach ($required_fields as $field) {
        if (!isset($input[$field]) || empty($input[$field])) {
            throw new Exception("Missing required field: $field");
        }
    }
    
    $user_id = intval($input['user_id']);
    $latitude = floatval($input['latitude']);
    $longitude = floatval($input['longitude']);
    $emergency_type = $input['emergency_type'] ?? 'emergency';
    $custom_message = $input['message'] ?? '';
    $contacts = is_array($input['contacts']) ? $input['contacts'] : [$input['contacts']];
    
    // Validate coordinates
    if ($latitude < -90 || $latitude > 90 || $longitude < -180 || $longitude > 180) {
        throw new Exception('Invalid coordinates provided');
    }
    
    // Get reverse geocoding information
    $location_info = getReverseGeocoding($latitude, $longitude);
    
    // Generate emergency message
    $emergency_message = generateEmergencyMessage($latitude, $longitude, $location_info, $emergency_type, $custom_message);
    
    // Send SMS to all contacts
    $results = [];
    $success_count = 0;
    $failure_count = 0;
    
    foreach ($contacts as $contact) {
        $phone = cleanPhoneNumber($contact);
        if (!$phone) {
            $results[] = [
                'phone' => $contact,
                'success' => false,
                'message' => 'Invalid phone number format'
            ];
            $failure_count++;
            continue;
        }
        
        $sms_result = sendSMS($phone, $emergency_message);
        $results[] = [
            'phone' => $phone,
            'success' => $sms_result['success'],
            'message' => $sms_result['message']
        ];
        
        if ($sms_result['success']) {
            $success_count++;
        } else {
            $failure_count++;
        }
    }
    
    // Log the emergency SMS attempt
    logEmergencySMS($user_id, $latitude, $longitude, $emergency_type, $contacts, $success_count, $failure_count);
    
    // Return response
    echo json_encode([
        'success' => $success_count > 0,
        'message' => "Emergency SMS sent to $success_count out of " . count($contacts) . " contacts",
        'data' => [
            'total_contacts' => count($contacts),
            'successful_sends' => $success_count,
            'failed_sends' => $failure_count,
            'location_info' => $location_info,
            'results' => $results
        ]
    ]);
    
} catch (Exception $e) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
}

/**
 * Get reverse geocoding information using OpenStreetMap Nominatim API
 */
function getReverseGeocoding($latitude, $longitude) {
    try {
        $url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=18&addressdetails=1";
        
        $context = stream_context_create([
            'http' => [
                'header' => "User-Agent: GzingApp-Emergency/1.0\r\n",
                'timeout' => 10
            ]
        ]);
        
        $response = file_get_contents($url, false, $context);
        $data = json_decode($response, true);
        
        if ($data && isset($data['display_name'])) {
            return [
                'address' => $data['display_name'],
                'formatted_address' => formatAddress($data),
                'place_type' => $data['type'] ?? 'unknown',
                'confidence' => $data['importance'] ?? 0
            ];
        }
    } catch (Exception $e) {
        // Fallback if geocoding fails
    }
    
    return [
        'address' => "Location: $latitude, $longitude",
        'formatted_address' => "Coordinates: $latitude, $longitude",
        'place_type' => 'unknown',
        'confidence' => 0
    ];
}

/**
 * Format address from geocoding data
 */
function formatAddress($data) {
    $address = $data['address'] ?? [];
    $parts = [];
    
    // Try to build a readable address
    if (isset($address['house_number']) && isset($address['road'])) {
        $parts[] = $address['house_number'] . ' ' . $address['road'];
    } elseif (isset($address['road'])) {
        $parts[] = $address['road'];
    }
    
    if (isset($address['suburb'])) {
        $parts[] = $address['suburb'];
    } elseif (isset($address['neighbourhood'])) {
        $parts[] = $address['neighbourhood'];
    }
    
    if (isset($address['city'])) {
        $parts[] = $address['city'];
    } elseif (isset($address['town'])) {
        $parts[] = $address['town'];
    }
    
    if (isset($address['state'])) {
        $parts[] = $address['state'];
    }
    
    if (isset($address['country'])) {
        $parts[] = $address['country'];
    }
    
    return !empty($parts) ? implode(', ', $parts) : $data['display_name'];
}

/**
 * Generate emergency message with location information
 */
function generateEmergencyMessage($latitude, $longitude, $location_info, $emergency_type, $custom_message) {
    $timestamp = date('Y-m-d H:i:s');
    $google_maps_link = "https://maps.google.com/maps?q=$latitude,$longitude";
    
    $message = "🚨 EMERGENCY ALERT 🚨\n\n";
    
    if (!empty($custom_message)) {
        $message .= "Message: $custom_message\n\n";
    }
    
    $message .= "📍 Location Details:\n";
    $message .= "Address: " . $location_info['formatted_address'] . "\n";
    $message .= "Coordinates: $latitude, $longitude\n";
    $message .= "Google Maps: $google_maps_link\n\n";
    
    $message .= "⏰ Time: $timestamp\n";
    $message .= "🚨 Emergency Type: " . ucfirst($emergency_type) . "\n\n";
    
    $message .= "Please help immediately! This is an automated emergency message from GzingApp.";
    
    return $message;
}

/**
 * Clean and validate phone number
 */
function cleanPhoneNumber($phone) {
    // Remove all non-numeric characters
    $phone = preg_replace('/[^0-9]/', '', $phone);
    
    // Handle Philippine phone numbers
    if (strlen($phone) == 10 && substr($phone, 0, 1) == '9') {
        // Add country code for Philippine mobile numbers
        $phone = '63' . $phone;
    } elseif (strlen($phone) == 11 && substr($phone, 0, 2) == '09') {
        // Remove leading 0 and add country code
        $phone = '63' . substr($phone, 1);
    } elseif (strlen($phone) == 12 && substr($phone, 0, 3) == '639') {
        // Already has country code
        $phone = $phone;
    } elseif (strlen($phone) == 13 && substr($phone, 0, 4) == '+639') {
        // Remove + sign
        $phone = substr($phone, 1);
    }
    
    // Validate final format (should be 12 digits for Philippine numbers)
    if (strlen($phone) == 12 && substr($phone, 0, 2) == '63') {
        return $phone;
    }
    
    return false;
}

/**
 * Send SMS using PhilSMS API
 */
function sendSMS($phone, $message) {
    global $SMS_API_TOKEN, $SMS_API_URL;
    
    try {
        $data = [
            'api_key' => $SMS_API_TOKEN,
            'number' => $phone,
            'message' => $message,
            'sender_id' => 'GzingApp'
        ];
        
        $options = [
            'http' => [
                'header' => "Content-type: application/x-www-form-urlencoded\r\n",
                'method' => 'POST',
                'content' => http_build_query($data),
                'timeout' => 30
            ]
        ];
        
        $context = stream_context_create($options);
        $response = file_get_contents($SMS_API_URL, false, $context);
        
        if ($response === false) {
            throw new Exception('Failed to connect to SMS service');
        }
        
        $result = json_decode($response, true);
        
        if ($result && isset($result['status']) && $result['status'] === 'success') {
            return [
                'success' => true,
                'message' => 'SMS sent successfully',
                'sms_id' => $result['sms_id'] ?? null
            ];
        } else {
            return [
                'success' => false,
                'message' => $result['message'] ?? 'Unknown error occurred'
            ];
        }
        
    } catch (Exception $e) {
        return [
            'success' => false,
            'message' => 'SMS service error: ' . $e->getMessage()
        ];
    }
}

/**
 * Log emergency SMS attempt to database
 */
function logEmergencySMS($user_id, $latitude, $longitude, $emergency_type, $contacts, $success_count, $failure_count) {
    try {
        // Try to connect to database
        if (class_exists('Database')) {
            $db = new Database();
            $pdo = $db->connect();
            
            if ($pdo) {
                $stmt = $pdo->prepare("
                    INSERT INTO emergency_sms_logs 
                    (user_id, latitude, longitude, emergency_type, contacts_json, success_count, failure_count, created_at) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, NOW())
                ");
                
                $stmt->execute([
                    $user_id,
                    $latitude,
                    $longitude,
                    $emergency_type,
                    json_encode($contacts),
                    $success_count,
                    $failure_count
                ]);
            }
        }
    } catch (Exception $e) {
        // Log error but don't fail the main operation
        error_log("Failed to log emergency SMS: " . $e->getMessage());
    }
}
?>

