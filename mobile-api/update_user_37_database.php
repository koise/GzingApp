<?php
/**
 * Update User 37 Database
 * Direct database update to add phone number to user 37
 */

echo "=== UPDATING USER 37 DATABASE ===\n\n";

// Database configuration for the deployed server
$host = 'localhost';
$dbname = 'u126959096_gzing_admin';
$username = 'u126959096_gzing_admin';
$password = 'X6v8M$U9;j';

try {
    // Connect to database
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "✅ Database connected successfully\n";
    
    // Check current user 37 data
    $checkStmt = $pdo->prepare("SELECT id, first_name, last_name, phone_number FROM users WHERE id = ?");
    $checkStmt->execute([37]);
    $user = $checkStmt->fetch(PDO::FETCH_ASSOC);
    
    if ($user) {
        echo "📋 Current User 37 Data:\n";
        echo "ID: " . $user['id'] . "\n";
        echo "Name: " . $user['first_name'] . " " . $user['last_name'] . "\n";
        echo "Phone: " . ($user['phone_number'] ?: 'NULL') . "\n\n";
        
        if (empty($user['phone_number'])) {
            echo "🔧 Adding phone number to User 37...\n";
            
            // Update user 37 with phone number
            $updateStmt = $pdo->prepare("UPDATE users SET phone_number = ? WHERE id = ?");
            $result = $updateStmt->execute(['+639171234567', 37]);
            
            if ($result) {
                echo "✅ Phone number added successfully!\n";
                echo "✅ User 37 now has phone number: +639171234567\n";
                
                // Verify the update
                $verifyStmt = $pdo->prepare("SELECT id, first_name, last_name, phone_number FROM users WHERE id = ?");
                $verifyStmt->execute([37]);
                $updatedUser = $verifyStmt->fetch(PDO::FETCH_ASSOC);
                
                echo "\n📋 Updated User 37 Data:\n";
                echo "ID: " . $updatedUser['id'] . "\n";
                echo "Name: " . $updatedUser['first_name'] . " " . $updatedUser['last_name'] . "\n";
                echo "Phone: " . $updatedUser['phone_number'] . "\n";
                
                echo "\n🎉 USER 37 FIXED! SMS service should now work with User ID 37!\n";
            } else {
                echo "❌ Failed to update user 37\n";
            }
        } else {
            echo "✅ User 37 already has phone number: " . $user['phone_number'] . "\n";
        }
    } else {
        echo "❌ User 37 not found in database\n";
    }
    
} catch (PDOException $e) {
    echo "❌ Database error: " . $e->getMessage() . "\n";
} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}

echo "\n=== UPDATE COMPLETE ===\n";
?>



