<?php
/**
 * Check User Data
 * Check what data exists for user ID 37
 */

echo "=== CHECKING USER DATA FOR ID 37 ===\n\n";

$baseUrl = 'https://powderblue-pig-261057.hostingersite.com/';
$hosts = 'localhost';
$dbname = 'u126959096_gzing_admin';
$username = 'u126959096_gzing_admin';
$password = 'X6v8M$U9;j';

$pdo = null;
foreach ($hosts as $host) {
    try {
        $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        break;
    } catch (PDOException $e) {
        continue;
    }
}

if (!$pdo) {
    echo "❌ Database connection failed\n";
    exit();
}

try {
    // Check user data
    $stmt = $pdo->prepare("SELECT id, first_name, last_name, email, phone_number, username, status FROM users WHERE id = ?");
    $stmt->execute([37]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if ($user) {
        echo "✅ User found:\n";
        echo "ID: " . $user['id'] . "\n";
        echo "Name: " . $user['first_name'] . " " . $user['last_name'] . "\n";
        echo "Email: " . $user['email'] . "\n";
        echo "Phone: " . ($user['phone_number'] ?: 'NULL') . "\n";
        echo "Username: " . $user['username'] . "\n";
        echo "Status: " . $user['status'] . "\n\n";
        
        if (empty($user['phone_number'])) {
            echo "❌ PHONE NUMBER MISSING!\n";
            echo "🔧 SOLUTION: Add phone number to user record\n\n";
            
            // Show available phone numbers from other users
            $phoneStmt = $pdo->prepare("SELECT id, first_name, last_name, phone_number FROM users WHERE phone_number IS NOT NULL AND phone_number != '' LIMIT 5");
            $phoneStmt->execute();
            $usersWithPhone = $phoneStmt->fetchAll(PDO::FETCH_ASSOC);
            
            echo "📱 Available phone numbers from other users:\n";
            foreach ($usersWithPhone as $userWithPhone) {
                echo "- ID {$userWithPhone['id']}: {$userWithPhone['first_name']} {$userWithPhone['last_name']} - {$userWithPhone['phone_number']}\n";
            }
        } else {
            echo "✅ Phone number exists: " . $user['phone_number'] . "\n";
        }
    } else {
        echo "❌ User not found with ID 37\n";
    }
    
} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}

echo "\n=== CHECK COMPLETE ===\n";
?>
