# Fetch User Details - Examples

## 🚀 API Endpoint
```
GET https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id={user_id}
```

## 📱 Usage Examples

### 1. cURL Command
```bash
# Fetch user with ID 9
curl "https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=9"

# Fetch user with ID 1
curl "https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=1"

# Fetch user with ID 33
curl "https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=33"
```

### 2. JavaScript (Browser)
```javascript
async function fetchUser(userId) {
    try {
        const response = await fetch(`https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=${userId}`);
        const data = await response.json();
        
        if (data.success) {
            console.log('User found:', data.data.user);
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
fetchUser(9).then(user => {
    if (user) {
        console.log(`User: ${user.first_name} ${user.last_name}`);
        console.log(`Email: ${user.email}`);
    }
});
```

### 3. JavaScript (Node.js)
```javascript
const https = require('https');

function fetchUser(userId) {
    return new Promise((resolve, reject) => {
        const url = `https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=${userId}`;
        
        https.get(url, (res) => {
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
        }).on('error', (error) => {
            reject(error);
        });
    });
}

// Usage
fetchUser(9).then(data => {
    if (data.success) {
        console.log('User:', data.data.user);
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
function fetchUser($userId) {
    $url = "https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=" . $userId;
    
    $response = file_get_contents($url);
    $data = json_decode($response, true);
    
    if ($data['success']) {
        return $data['data']['user'];
    } else {
        return null;
    }
}

// Usage
$user = fetchUser(9);
if ($user) {
    echo "User: " . $user['first_name'] . " " . $user['last_name'] . "\n";
    echo "Email: " . $user['email'] . "\n";
} else {
    echo "User not found\n";
}
?>
```

### 5. Python
```python
import requests
import json

def fetch_user(user_id):
    url = f"https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id={user_id}"
    
    try:
        response = requests.get(url)
        data = response.json()
        
        if data['success']:
            return data['data']['user']
        else:
            return None
    except Exception as e:
        print(f"Error: {e}")
        return None

# Usage
user = fetch_user(9)
if user:
    print(f"User: {user['first_name']} {user['last_name']}")
    print(f"Email: {user['email']}")
else:
    print("User not found")
```

## 📊 Response Format

### Success Response
```json
{
    "success": true,
    "message": "User data retrieved successfully",
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
            "updated_at": "2025-01-12 10:30:00",
            "last_login": null
        }
    },
    "timestamp": "2025-01-12 10:30:00"
}
```

### Error Response
```json
{
    "success": false,
    "message": "User not found",
    "data": null,
    "timestamp": "2025-01-12 10:30:00"
}
```

## 🧪 Test URLs

- **User 1**: https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=1
- **User 9**: https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=9
- **User 33**: https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/getuser.php?id=33
- **Demo Page**: https://powderblue-pig-261057.hostingersite.com/mobile-api/prototype/fetch_user_demo.html

## 🔧 Features

- ✅ **No Authentication Required** - Perfect for prototyping
- ✅ **CORS Enabled** - Works with web browsers
- ✅ **JSON Response** - Easy to parse
- ✅ **Error Handling** - Clear error messages
- ✅ **Database Connected** - Uses correct database credentials
- ✅ **Clean URLs** - Simple parameter format

