<?php
/**
 * Session Manager Class
 * Handles session-based authentication
 */

class SessionManager {
    
    public static function startSession() {
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }
    }
    
    public static function isLoggedIn() {
        self::startSession();
        return isset($_SESSION['user_id']) && isset($_SESSION['user_email']);
    }
    
    public static function getUserId() {
        self::startSession();
        return $_SESSION['user_id'] ?? null;
    }
    
    public static function getUserEmail() {
        self::startSession();
        return $_SESSION['user_email'] ?? null;
    }
    
    public static function getUserRole() {
        self::startSession();
        return $_SESSION['user_role'] ?? 'user';
    }
    
    public static function getUserData() {
        self::startSession();
        return [
            'id' => $_SESSION['user_id'] ?? null,
            'email' => $_SESSION['user_email'] ?? null,
            'username' => $_SESSION['user_username'] ?? null,
            'first_name' => $_SESSION['user_first_name'] ?? null,
            'last_name' => $_SESSION['user_last_name'] ?? null,
            'role' => $_SESSION['user_role'] ?? 'user',
            'phone_number' => $_SESSION['user_phone_number'] ?? null
        ];
    }
    
    public static function login($userData) {
        self::startSession();
        
        // Regenerate session ID for security
        session_regenerate_id(true);
        
        $_SESSION['user_id'] = $userData['id'];
        $_SESSION['user_email'] = $userData['email'];
        $_SESSION['user_username'] = $userData['username'];
        $_SESSION['user_first_name'] = $userData['first_name'];
        $_SESSION['user_last_name'] = $userData['last_name'];
        $_SESSION['user_role'] = $userData['role'];
        $_SESSION['user_phone_number'] = $userData['phone_number'];
        $_SESSION['login_time'] = time();
        $_SESSION['last_activity'] = time();
        
        return true;
    }
    
    public static function logout() {
        self::startSession();
        
        // Clear all session variables
        $_SESSION = array();
        
        // Destroy the session cookie
        if (ini_get("session.use_cookies")) {
            $params = session_get_cookie_params();
            setcookie(session_name(), '', time() - 42000,
                $params["path"], $params["domain"],
                $params["secure"], $params["httponly"]
            );
        }
        
        // Destroy the session
        session_destroy();
        
        return true;
    }
    
    public static function updateActivity() {
        self::startSession();
        $_SESSION['last_activity'] = time();
    }
    
    public static function isSessionExpired($timeout = 3600) { // 1 hour default
        self::startSession();
        
        if (!isset($_SESSION['last_activity'])) {
            return true;
        }
        
        return (time() - $_SESSION['last_activity']) > $timeout;
    }
    
    public static function requireAuth() {
        if (!self::isLoggedIn()) {
            Response::unauthorized('Authentication required');
        }
        
        if (self::isSessionExpired()) {
            self::logout();
            Response::unauthorized('Session expired');
        }
        
        self::updateActivity();
    }
    
    public static function requireRole($requiredRole) {
        self::requireAuth();
        
        $userRole = self::getUserRole();
        $roleHierarchy = ['user' => 1, 'moderator' => 2, 'admin' => 3];
        
        if (!isset($roleHierarchy[$userRole]) || 
            $roleHierarchy[$userRole] < $roleHierarchy[$requiredRole]) {
            Response::forbidden('Insufficient permissions');
        }
    }
}
?>

