<?php
/**
 * Validation Helper Class
 * Input validation utilities
 */

class Validator {
    
    public static function validateEmail($email) {
        return filter_var($email, FILTER_VALIDATE_EMAIL) !== false;
    }
    
    public static function validatePhone($phone) {
        // Philippine phone number format: +639XXXXXXXXX or 09XXXXXXXXX
        $pattern = '/^(\+639|09)[0-9]{9}$/';
        return preg_match($pattern, $phone);
    }
    
    public static function validatePassword($password) {
        // At least 6 characters
        return strlen($password) >= 6;
    }
    
    public static function validateRequired($fields, $data) {
        $errors = [];
        
        foreach ($fields as $field) {
            if (!isset($data[$field]) || empty(trim($data[$field]))) {
                $errors[$field] = ucfirst($field) . ' is required';
            }
        }
        
        return $errors;
    }
    
    public static function sanitizeString($string) {
        return htmlspecialchars(strip_tags(trim($string)), ENT_QUOTES, 'UTF-8');
    }
    
    public static function sanitizeEmail($email) {
        return filter_var(trim($email), FILTER_SANITIZE_EMAIL);
    }
    
    public static function sanitizePhone($phone) {
        // Remove all non-numeric characters except +
        $phone = preg_replace('/[^0-9+]/', '', $phone);
        
        // Convert to Philippine format if needed
        if (strlen($phone) === 11 && substr($phone, 0, 2) === '09') {
            $phone = '+63' . substr($phone, 1);
        }
        
        return $phone;
    }
    
    public static function validateCoordinates($lat, $lng) {
        return is_numeric($lat) && is_numeric($lng) && 
               $lat >= -90 && $lat <= 90 && 
               $lng >= -180 && $lng <= 180;
    }
    
    public static function validateJson($jsonString) {
        json_decode($jsonString);
        return json_last_error() === JSON_ERROR_NONE;
    }
}
?>

