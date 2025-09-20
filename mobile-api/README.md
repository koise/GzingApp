# Gzing Mobile API

A session-based REST API for the Gzing transportation mobile application.

## Features

- **Session-based Authentication** (no tokens required)
- **User Management** (registration, login, logout)
- **Route Management** (CRUD operations)
- **SOS Contacts** (emergency contacts management)
- **Navigation Logging** (activity tracking)
- **Input Validation** and **Error Handling**
- **Pagination Support**
- **Search and Filtering**

## API Endpoints

### Authentication
- `POST /auth/login` - User login
- `POST /auth/signup` - User registration
- `POST /auth/logout` - User logout
- `GET /auth/check` - Check session validity

### Users
- `GET /users` - Get users list (moderator/admin only)
- `POST /users` - Create new user (moderator/admin only)

### Routes
- `GET /routes` - Get routes list
- `POST /routes` - Create new route

### SOS Contacts
- `GET /sos-contacts` - Get user SOS contacts
- `POST /sos-contacts` - Create new SOS contact

### Navigation Logs
- `GET /navigation-logs` - Get navigation activity logs
- `POST /navigation-logs` - Create navigation activity log

### System
- `GET /health` - API health check
- `GET /info` - API information

## Installation

1. **Database Setup**
   - Import the SQL file: `config/u126959096_gzing_admin (2).sql`
   - Update database credentials in `config/database.php`

2. **Web Server Configuration**
   - Place files in your web server directory
   - Ensure PHP 7.4+ is installed
   - Enable PDO and JSON extensions

3. **Permissions**
   - Ensure web server has read/write access to the directory
   - Session directory should be writable

## Usage

### Authentication Flow

1. **Signup/Login**: User provides credentials
2. **Session Creation**: Server creates PHP session with user data
3. **Cookie Management**: Browser automatically handles session cookies
4. **API Calls**: All subsequent requests include session cookie
5. **Logout**: Session is destroyed on server and client

### Example Requests

#### Login
```bash
curl -X POST http://your-domain/mobile-api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

#### Get Routes (authenticated)
```bash
curl -X GET http://your-domain/mobile-api/routes \
  -H "Cookie: PHPSESSID=your_session_id"
```

#### Create SOS Contact
```bash
curl -X POST http://your-domain/mobile-api/sos-contacts \
  -H "Content-Type: application/json" \
  -H "Cookie: PHPSESSID=your_session_id" \
  -d '{"name":"Emergency Contact","phone_number":"+639123456789","relationship":"Family","is_primary":true}'
```

## Response Format

All API responses follow this format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2025-01-12 10:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2025-01-12 10:30:00"
}
```

## Database Schema

The API uses the following main tables:
- `users` - User accounts and profiles
- `routes` - Transportation routes with map data
- `sos_contacts` - Emergency contacts
- `navigation_activity_logs` - Navigation tracking
- `user_activity_logs` - System activity logs

## Security Features

- **Session Management**: Secure session handling with regeneration
- **Input Validation**: Comprehensive input sanitization
- **SQL Injection Protection**: Prepared statements
- **XSS Protection**: Output escaping
- **Role-based Access**: User roles (user, moderator, admin)
- **Activity Logging**: All actions are logged

## Testing

Run the test script to verify API functionality:

```bash
php test_api.php
```

## Android Integration

The Android app uses Retrofit with cookie management for session handling:

1. **RetrofitClient**: Configured with cookie jar for session management
2. **AuthRepository**: Handles login, signup, logout, and session checking
3. **Session Management**: Automatic session validation in SplashActivity
4. **Logout**: Clears both server session and local cookies

## Error Codes

- `200` - Success
- `400` - Bad Request
- `401` - Unauthorized (invalid credentials or expired session)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `405` - Method Not Allowed
- `422` - Validation Error
- `500` - Internal Server Error

## Configuration

### Database Configuration
Update `config/database.php` with your database credentials:

```php
$this->config = [
    'host' => 'your-host',
    'dbname' => 'your-database',
    'username' => 'your-username',
    'password' => 'your-password',
    // ...
];
```

### Session Configuration
Sessions are configured in PHP.ini or via `session_start()` parameters.

## Support

For issues or questions, please check the logs in the database `user_activity_logs` table or contact the development team.

