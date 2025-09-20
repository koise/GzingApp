<?php
/**
 * Mobile API Main Router
 * Handles all API requests and routes them to appropriate endpoints
 */

// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Set CORS headers
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
header('Access-Control-Allow-Credentials: true');
header('Content-Type: application/json');

// Handle preflight OPTIONS requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Get the request URI and method
$requestUri = $_SERVER['REQUEST_URI'];
$requestMethod = $_SERVER['REQUEST_METHOD'];

// Remove query string from URI
$path = parse_url($requestUri, PHP_URL_PATH);

// Remove the base path (mobile-api)
$path = str_replace('/mobile-api', '', $path);
$path = trim($path, '/');

// Split path into segments
$segments = explode('/', $path);

// Route the request
try {
    // Default route - API info
    if (empty($path) || $path === '') {
        require_once __DIR__ . '/endpoints/info.php';
        exit();
    }
    
    // Routes endpoints
    if ($segments[0] === 'routes') {
        if ($requestMethod === 'GET') {
            // GET /routes - Get all routes
            if (count($segments) === 1) {
                require_once __DIR__ . '/endpoints/routes/get_routes.php';
                exit();
            }
            // GET /routes/{id} - Get specific route
            elseif (count($segments) === 2 && is_numeric($segments[1])) {
                $_GET['id'] = $segments[1];
                require_once __DIR__ . '/endpoints/routes/get_route.php';
                exit();
            }
        }
        elseif ($requestMethod === 'POST') {
            // POST /routes - Create new route
            require_once __DIR__ . '/endpoints/routes/create_route.php';
            exit();
        }
    }
    
    // Navigation endpoints
    elseif ($segments[0] === 'navigation') {
        if ($requestMethod === 'GET') {
            // GET /navigation - Get navigation logs
            if (count($segments) === 1) {
                require_once __DIR__ . '/endpoints/navigation/get_navigation_logs.php';
                exit();
            }
            // GET /navigation/stats - Get navigation stats
            elseif (count($segments) === 2 && $segments[1] === 'stats') {
                require_once __DIR__ . '/endpoints/navigation/get_navigation_stats.php';
                exit();
            }
            // GET /navigation/{id} - Get specific navigation log
            elseif (count($segments) === 2 && is_numeric($segments[1])) {
                $_GET['log_id'] = $segments[1];
                require_once __DIR__ . '/endpoints/navigation/get_navigation_log_detail.php';
                exit();
            }
        }
        elseif ($requestMethod === 'POST') {
            // POST /navigation - Create navigation log
            if (count($segments) === 1) {
                require_once __DIR__ . '/endpoints/navigation/create_navigation_log.php';
                exit();
            }
            // POST /navigation/stop - Stop navigation
            elseif (count($segments) === 2 && $segments[1] === 'stop') {
                require_once __DIR__ . '/endpoints/navigation/stop_navigation.php';
                exit();
            }
        }
        elseif ($requestMethod === 'PUT') {
            // PUT /navigation/{id} - Update navigation log
            if (count($segments) === 2 && is_numeric($segments[1])) {
                $_GET['log_id'] = $segments[1];
                require_once __DIR__ . '/endpoints/navigation/update_navigation_log.php';
                exit();
            }
        }
    }
    
        // Navigation History endpoints
        elseif ($segments[0] === 'navigation-history') {
            if ($requestMethod === 'GET') {
                // GET /navigation-history - Get all navigation history for user
                if (count($segments) === 1) {
                    require_once __DIR__ . '/endpoints/navigation-history/get_navigation_history.php';
                    exit();
                }
                // GET /navigation-history/stats - Get navigation history stats
                elseif (count($segments) === 2 && $segments[1] === 'stats') {
                    require_once __DIR__ . '/endpoints/navigation-history/get_navigation_history_stats.php';
                    exit();
                }
                // GET /navigation-history/{id} - Get specific navigation history by ID
                elseif (count($segments) === 2 && is_numeric($segments[1])) {
                    $_GET['id'] = $segments[1];
                    require_once __DIR__ . '/endpoints/navigation-history/get_navigation_history_by_id.php';
                    exit();
                }
            }
            elseif ($requestMethod === 'POST') {
                // POST /navigation-history - Create navigation history
                if (count($segments) === 1) {
                    require_once __DIR__ . '/endpoints/navigation-history/create_navigation_history_standalone.php';
                    exit();
                }
            }
        }
        
        // SMS endpoints
        elseif ($segments[0] === 'sms') {
            if ($requestMethod === 'POST') {
                // POST /sms/send_emergency_sms - Send emergency SMS
                if (count($segments) === 2 && $segments[1] === 'send_emergency_sms') {
                    require_once __DIR__ . '/endpoints/sms/send_emergency_sms.php';
                    exit();
                }
            }
        }
    
    // Navigation Activity Logs endpoints (for backward compatibility)
    elseif ($segments[0] === 'navigation_activity_logs') {
        if ($requestMethod === 'GET') {
            // GET /navigation_activity_logs/user-logs - Get user navigation logs
            if (count($segments) === 2 && $segments[1] === 'user-logs') {
                require_once __DIR__ . '/endpoints/navigation/get_navigation_logs.php';
                exit();
            }
            // GET /navigation_activity_logs/stats - Get navigation stats
            elseif (count($segments) === 2 && $segments[1] === 'stats') {
                require_once __DIR__ . '/endpoints/navigation/get_navigation_stats.php';
                exit();
            }
        }
    }
    
    // Handle duplicate mobile-api/endpoints paths
    elseif ($segments[0] === 'mobile-api' && count($segments) > 1) {
        // Remove the duplicate 'mobile-api' and 'endpoints' from the path
        $newSegments = array_slice($segments, 2); // Skip 'mobile-api' and 'endpoints'
        $newPath = implode('/', $newSegments);
        
        // Handle different HTTP methods for navigation endpoints
        if ($newPath === 'navigation') {
            if ($requestMethod === 'GET') {
                require_once __DIR__ . '/endpoints/navigation/get_navigation_logs.php';
                exit();
            }
            elseif ($requestMethod === 'POST') {
                require_once __DIR__ . '/endpoints/navigation/create_navigation_log.php';
                exit();
            }
        }
        elseif ($newPath === 'navigation/stats') {
            if ($requestMethod === 'GET') {
                require_once __DIR__ . '/endpoints/navigation/get_navigation_stats.php';
                exit();
            }
        }
    }
    
    // Handle direct endpoints paths (without mobile-api prefix)
    elseif ($segments[0] === 'endpoints' && count($segments) > 1) {
        $newSegments = array_slice($segments, 1); // Skip 'endpoints'
        $newPath = implode('/', $newSegments);
        
        // Handle different HTTP methods for navigation endpoints
        if ($newPath === 'navigation') {
            if ($requestMethod === 'GET') {
                require_once __DIR__ . '/endpoints/navigation/get_navigation_logs.php';
                exit();
            }
            elseif ($requestMethod === 'POST') {
                require_once __DIR__ . '/endpoints/navigation/create_navigation_log.php';
                exit();
            }
        }
        elseif ($newPath === 'navigation/stats') {
            if ($requestMethod === 'GET') {
                require_once __DIR__ . '/endpoints/navigation/get_navigation_stats.php';
                exit();
            }
        }
    }
    
    // Auth endpoints
    elseif ($segments[0] === 'auth') {
        if ($requestMethod === 'POST') {
            // POST /auth/login
            if (count($segments) === 2 && $segments[1] === 'login') {
                require_once __DIR__ . '/endpoints/auth/login.php';
                exit();
            }
            // POST /auth/signup
            elseif (count($segments) === 2 && $segments[1] === 'signup') {
                require_once __DIR__ . '/endpoints/auth/signup.php';
                exit();
            }
            // POST /auth/logout
            elseif (count($segments) === 2 && $segments[1] === 'logout') {
                require_once __DIR__ . '/endpoints/auth/logout.php';
                exit();
            }
        }
        elseif ($requestMethod === 'GET') {
            // GET /auth/check
            if (count($segments) === 2 && $segments[1] === 'check') {
                require_once __DIR__ . '/endpoints/auth/check.php';
                exit();
            }
        }
    }
    
    // Users endpoints
    elseif ($segments[0] === 'users') {
        if ($requestMethod === 'GET') {
            // GET /users - Get all users
            require_once __DIR__ . '/endpoints/users/get_users.php';
            exit();
        }
        elseif ($requestMethod === 'POST') {
            // POST /users - Create new user
            require_once __DIR__ . '/endpoints/users/create_user.php';
            exit();
        }
    }
    
    // SOS endpoints
    elseif ($segments[0] === 'sos') {
        if ($requestMethod === 'GET') {
            // GET /sos - Get SOS contacts
            require_once __DIR__ . '/endpoints/sos/get_contacts.php';
            exit();
        }
        elseif ($requestMethod === 'POST') {
            // POST /sos - Create SOS contact
            require_once __DIR__ . '/endpoints/sos/create_contact.php';
            exit();
        }
    }
    
    // Landmarks endpoints
    elseif ($segments[0] === 'landmarks') {
        if ($requestMethod === 'GET') {
            // GET /landmarks - Get all landmarks (active only)
            require_once __DIR__ . '/endpoints/landmarks/get_landmarks.php';
            exit();
        }
    }
    
    // Health check
    elseif ($segments[0] === 'health') {
        require_once __DIR__ . '/endpoints/health.php';
        exit();
    }
    
    // If no route matches, return 404
    http_response_code(404);
    echo json_encode([
        'success' => false,
        'message' => 'Endpoint not found',
        'path' => $path,
        'method' => $requestMethod,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Internal server error: ' . $e->getMessage(),
        'timestamp' => date('Y-m-d H:i:s')
    ]);
}
?>