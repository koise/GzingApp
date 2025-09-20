<?php
/**
 * API Info Endpoint
 * GET /info
 */

require_once '../../config/database.php';
require_once '../../includes/Response.php';

try {
    $endpoints = [
        'auth' => [
            'POST /auth/login' => 'User login with email/username and password',
            'POST /auth/signup' => 'User registration',
            'POST /auth/logout' => 'User logout',
            'GET /auth/check' => 'Check session validity'
        ],
        'users' => [
            'GET /users' => 'Get users list (moderator/admin only)',
            'POST /users' => 'Create new user (moderator/admin only)'
        ],
        'routes' => [
            'GET /routes' => 'Get routes list',
            'POST /routes' => 'Create new route'
        ],
        'sos_contacts' => [
            'GET /sos-contacts' => 'Get user SOS contacts',
            'POST /sos-contacts' => 'Create new SOS contact'
        ],
        'navigation' => [
            'GET /navigation-logs' => 'Get navigation activity logs',
            'POST /navigation-logs' => 'Create navigation activity log'
        ],
        'system' => [
            'GET /health' => 'API health check',
            'GET /info' => 'API information'
        ]
    ];
    
    Response::success([
        'api_name' => 'Gzing Mobile API',
        'version' => '1.0.0',
        'description' => 'Session-based mobile API for Gzing transportation app',
        'authentication' => 'Session-based (no tokens)',
        'base_url' => 'http://' . $_SERVER['HTTP_HOST'] . '/mobile-api',
        'endpoints' => $endpoints,
        'features' => [
            'Session-based authentication',
            'User management',
            'Route management',
            'SOS contacts',
            'Navigation logging',
            'Activity logging',
            'Pagination support',
            'Search and filtering',
            'Input validation',
            'Error handling'
        ],
        'requirements' => [
            'PHP 7.4+',
            'MySQL/MariaDB',
            'PDO extension',
            'JSON extension'
        ]
    ], 'API information retrieved successfully');
    
} catch (Exception $e) {
    Response::serverError('Failed to get API info: ' . $e->getMessage());
}
?>

