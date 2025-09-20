<?php
/**
 * Response Helper Class
 * Standardized API responses
 */

class Response {
    
    public static function success($data = null, $message = 'Success', $code = 200) {
        http_response_code($code);
        echo json_encode([
            'success' => true,
            'message' => $message,
            'data' => $data,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    public static function error($message = 'Error', $code = 400, $data = null) {
        http_response_code($code);
        echo json_encode([
            'success' => false,
            'message' => $message,
            'data' => $data,
            'timestamp' => date('Y-m-d H:i:s')
        ]);
        exit();
    }
    
    public static function unauthorized($message = 'Unauthorized') {
        self::error($message, 401);
    }
    
    public static function forbidden($message = 'Forbidden') {
        self::error($message, 403);
    }
    
    public static function notFound($message = 'Not found') {
        self::error($message, 404);
    }
    
    public static function methodNotAllowed($message = 'Method not allowed') {
        self::error($message, 405);
    }
    
    public static function validationError($errors, $message = 'Validation failed') {
        self::error($message, 422, ['errors' => $errors]);
    }
    
    public static function serverError($message = 'Internal server error') {
        self::error($message, 500);
    }
}
?>

