<?php
/**
 * Database Configuration for Mobile API
 * Uses the same database as the main application
 */

class Database {
    private $connection;
    private $config;
    
    public function __construct() {
        // Try to get database config from environment variables first
        $this->config = [
            'host' => $_ENV['DB_HOST'] ?? 'localhost',
            'dbname' => $_ENV['DB_NAME'] ?? 'u126959096_gzing_admin',
            'username' => $_ENV['DB_USER'] ?? 'u126959096_gzing_admin',
            'password' => $_ENV['DB_PASS'] ?? 'X6v8M$U9;j',
            'charset' => 'utf8mb4',
            'options' => [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false,
            ]
        ];
        
        $this->connect();
    }
    
    private function connect() {
        try {
            // Create logs directory if it doesn't exist
            $logs_dir = __DIR__ . '/../logs';
            if (!is_dir($logs_dir)) {
                mkdir($logs_dir, 0755, true);
            }
            
            // For remote hosting, try different host configurations
            $hosts = [
                $this->config['host'],
                'localhost',
                '127.0.0.1'
            ];
            
            error_log("Database connection attempt - trying hosts: " . implode(', ', $hosts));
            error_log("Database config - host: {$this->config['host']}, dbname: {$this->config['dbname']}, username: {$this->config['username']}");
            
            $connected = false;
            foreach ($hosts as $host) {
                try {
                    $dsn = "mysql:host={$host};dbname={$this->config['dbname']};charset={$this->config['charset']}";
                    error_log("Attempting connection to: $dsn");
                    $this->connection = new PDO($dsn, $this->config['username'], $this->config['password'], $this->config['options']);
                    error_log("SUCCESS: Connected to database on host: $host");
                    $connected = true;
                    break;
                } catch (PDOException $e) {
                    error_log("FAILED: Connection to host $host failed: " . $e->getMessage());
                    // Try next host
                    continue;
                }
            }
            
            if (!$connected) {
                error_log("ERROR: Unable to connect to any database host");
                throw new Exception('Unable to connect to any database host');
            }
            
        } catch (Exception $e) {
            error_log("ERROR: Database connection failed: " . $e->getMessage());
            // For development/testing, create a simple in-memory response
            throw new Exception('Database connection failed: ' . $e->getMessage());
        }
    }
    
    public function getConnection() {
        return $this->connection;
    }
    
    public function query($sql, $params = []) {
        try {
            $stmt = $this->connection->prepare($sql);
            $stmt->execute($params);
            
            // For INSERT queries, return the last insert ID
            if (stripos(trim($sql), 'INSERT') === 0) {
                return $this->connection->lastInsertId();
            }
            
            return $stmt;
        } catch (PDOException $e) {
            throw new Exception('Query failed: ' . $e->getMessage());
        }
    }
    
    public function lastInsertId() {
        return $this->connection->lastInsertId();
    }
    
    public function beginTransaction() {
        return $this->connection->beginTransaction();
    }
    
    public function commit() {
        return $this->connection->commit();
    }
    
    public function rollback() {
        return $this->connection->rollback();
    }
}
?>
