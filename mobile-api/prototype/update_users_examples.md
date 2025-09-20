# Update Users API - Examples

## 🚀 API Endpoint
```
POST https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/updateUsers
```

## 📱 Usage Examples

### 1. cURL Command
```bash
# Update user with ID 9
curl -X POST "https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/updateUsers" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": 9,
    "first_name": "John",
    "last_name": "Doe",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "phone_number": "+1234567890",
    "role": "user"
  }'
```

### 2. JavaScript (Browser)
```javascript
async function updateUser(userId, userData) {
    try {
        const response = await fetch('https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/updateUsers', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                user_id: userId,
                first_name: userData.firstName,
                last_name: userData.lastName,
                email: userData.email,
                username: userData.username,
                phone_number: userData.phoneNumber,
                role: userData.role
            })
        });
        
        const data = await response.json();
        
        if (data.success) {
            console.log('User updated:', data.data.user);
            return data.data.user;
        } else {
            console.error('Error:', data.message);
            return null;
        }
    } catch (error) {
        console.error('Network error:', error);
        return null;
    }
}

// Usage
const userData = {
    firstName: 'Jane',
    lastName: 'Smith',
    email: 'jane.smith@example.com',
    username: 'janesmith',
    phoneNumber: '+1987654321',
    role: 'user'
};

updateUser(9, userData).then(updatedUser => {
    if (updatedUser) {
        console.log(`User updated: ${updatedUser.first_name} ${updatedUser.last_name}`);
    }
});
```

### 3. JavaScript (Node.js)
```javascript
const https = require('https');

function updateUser(userId, userData) {
    return new Promise((resolve, reject) => {
        const postData = JSON.stringify({
            user_id: userId,
            first_name: userData.firstName,
            last_name: userData.lastName,
            email: userData.email,
            username: userData.username,
            phone_number: userData.phoneNumber,
            role: userData.role
        });
        
        const options = {
            hostname: 'powderblue-pig-261057.hostingersite.com',
            path: '/mobile-api/prototype/updateUsers',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(postData)
            }
        };
        
        const req = https.request(options, (res) => {
            let data = '';
            
            res.on('data', (chunk) => {
                data += chunk;
            });
            
            res.on('end', () => {
                try {
                    const jsonData = JSON.parse(data);
                    resolve(jsonData);
                } catch (error) {
                    reject(error);
                }
            });
        });
        
        req.on('error', (error) => {
            reject(error);
        });
        
        req.write(postData);
        req.end();
    });
}

// Usage
const userData = {
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com',
    username: 'johndoe',
    phoneNumber: '+1234567890',
    role: 'user'
};

updateUser(9, userData).then(data => {
    if (data.success) {
        console.log('User updated:', data.data.user);
    } else {
        console.error('Error:', data.message);
    }
}).catch(error => {
    console.error('Error:', error);
});
```

### 4. PHP
```php
<?php
function updateUser($userId, $userData) {
    $url = "https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/updateUsers";
    
    $postData = json_encode([
        'user_id' => $userId,
        'first_name' => $userData['firstName'],
        'last_name' => $userData['lastName'],
        'email' => $userData['email'],
        'username' => $userData['username'],
        'phone_number' => $userData['phoneNumber'],
        'role' => $userData['role']
    ]);
    
    $context = stream_context_create([
        'http' => [
            'method' => 'POST',
            'header' => 'Content-Type: application/json',
            'content' => $postData
        ]
    ]);
    
    $response = file_get_contents($url, false, $context);
    $data = json_decode($response, true);
    
    if ($data['success']) {
        return $data['data']['user'];
    } else {
        return null;
    }
}

// Usage
$userData = [
    'firstName' => 'John',
    'lastName' => 'Doe',
    'email' => 'john.doe@example.com',
    'username' => 'johndoe',
    'phoneNumber' => '+1234567890',
    'role' => 'user'
];

$updatedUser = updateUser(9, $userData);
if ($updatedUser) {
    echo "User updated: " . $updatedUser['first_name'] . " " . $updatedUser['last_name'] . "\n";
} else {
    echo "Failed to update user\n";
}
?>
```

### 5. Python
```python
import requests
import json

def update_user(user_id, user_data):
    url = "https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/updateUsers"
    
    payload = {
        "user_id": user_id,
        "first_name": user_data["firstName"],
        "last_name": user_data["lastName"],
        "email": user_data["email"],
        "username": user_data["username"],
        "phone_number": user_data["phoneNumber"],
        "role": user_data["role"]
    }
    
    try:
        response = requests.post(url, json=payload)
        data = response.json()
        
        if data["success"]:
            return data["data"]["user"]
        else:
            return None
    except Exception as e:
        print(f"Error: {e}")
        return None

# Usage
user_data = {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "username": "johndoe",
    "phoneNumber": "+1234567890",
    "role": "user"
}

updated_user = update_user(9, user_data)
if updated_user:
    print(f"User updated: {updated_user['first_name']} {updated_user['last_name']}")
else:
    print("Failed to update user")
```

## 📊 Request Format

### Required Fields
```json
{
    "user_id": 9,
    "first_name": "John",
    "last_name": "Doe",
    "email": "john@example.com"
}
```

### Optional Fields
```json
{
    "username": "johndoe",
    "phone_number": "+1234567890",
    "role": "user"
}
```

## 📊 Response Format

### Success Response
```json
{
    "success": true,
    "message": "User updated successfully",
    "data": {
        "user": {
            "id": 9,
            "first_name": "John",
            "last_name": "Doe",
            "email": "john@example.com",
            "username": "johndoe",
            "phone_number": "+1234567890",
            "role": "user",
            "status": "active",
            "created_at": "2025-01-12 10:30:00",
            "updated_at": "2025-01-12 11:45:00",
            "last_login": null
        }
    },
    "timestamp": "2025-01-12 11:45:00"
}
```

### Error Response
```json
{
    "success": false,
    "message": "Email is already taken by another user",
    "data": null,
    "timestamp": "2025-01-12 11:45:00"
}
```

## 🧪 Test URLs

- **Test Page**: https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/test_update_users.html
- **API Documentation**: https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/index.php

## 🔧 Features

- ✅ **No Authentication Required** - Perfect for prototyping
- ✅ **CORS Enabled** - Works with web browsers
- ✅ **JSON Request/Response** - Easy to use
- ✅ **Validation** - Email format, required fields, duplicate checks
- ✅ **Error Handling** - Comprehensive error messages
- ✅ **Database Connected** - Uses correct database credentials
- ✅ **Clean URLs** - Simple parameter format

## ⚠️ Validation Rules

1. **Required Fields**: `user_id`, `first_name`, `last_name`, `email`
2. **Email Format**: Must be valid email format
3. **Email Uniqueness**: Email cannot be taken by another user
4. **Username Uniqueness**: Username cannot be taken by another user (if provided)
5. **User Existence**: User must exist and not be deleted

