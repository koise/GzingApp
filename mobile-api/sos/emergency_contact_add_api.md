# Emergency Contact Add API

## Overview
This API endpoint allows users to add emergency contacts to their profile with validation to prevent adding contacts with the same phone number as the user's own number.

## Endpoint
```
POST /mobile-api/sos/emergencycontactadd.php
```

## Request Headers
```
Content-Type: application/json
```

## Request Body
```json
{
  "user_id": 33,
  "name": "Emergency Contact Name",
  "phone_number": "+1234567890",
  "relationship": "Family",
  "is_primary": true
}
```

### Required Fields
- `user_id` (integer): The ID of the user adding the contact
- `name` (string): The name of the emergency contact
- `phone_number` (string): The phone number of the contact (10-15 digits, optional + prefix)
- `relationship` (string): Relationship to user (Family, Friend, Partner, Others)

### Optional Fields
- `is_primary` (boolean): Whether this is the primary emergency contact (default: false)

## Response Format

### Success Response (200)
```json
{
  "success": true,
  "message": "Emergency contact added successfully",
  "data": {
    "contact": {
      "id": 13,
      "user_id": 33,
      "name": "Emergency Contact Name",
      "phone_number": "+1234567890",
      "relationship": "Family",
      "is_primary": 1,
      "created_at": "2025-09-12 10:30:00",
      "updated_at": "2025-09-12 10:30:00"
    }
  },
  "timestamp": "2025-09-12 10:30:00"
}
```

### Error Responses

#### 400 - Bad Request
```json
{
  "success": false,
  "message": "Missing required field: name",
  "data": null,
  "timestamp": "2025-09-12 10:30:00"
}
```

#### 400 - Invalid Phone Number
```json
{
  "success": false,
  "message": "Invalid phone number format",
  "data": null,
  "timestamp": "2025-09-12 10:30:00"
}
```

#### 400 - Duplicate Phone Number
```json
{
  "success": false,
  "message": "Emergency contact with this phone number already exists",
  "data": null,
  "timestamp": "2025-09-12 10:30:00"
}
```

#### 400 - Own Phone Number
```json
{
  "success": false,
  "message": "Cannot add your own phone number as an emergency contact",
  "data": null,
  "timestamp": "2025-09-12 10:30:00"
}
```

#### 404 - User Not Found
```json
{
  "success": false,
  "message": "User not found",
  "data": null,
  "timestamp": "2025-09-12 10:30:00"
}
```

#### 500 - Server Error
```json
{
  "success": false,
  "message": "Database error occurred",
  "data": null,
  "timestamp": "2025-09-12 10:30:00"
}
```

## Validation Rules

### Phone Number Validation
- Must be 10-15 digits long
- Can optionally start with +
- Must match pattern: `^\+?[0-9]{10,15}$`

### Relationship Validation
- Must be one of: Family, Friend, Partner, Others
- Case-sensitive

### Duplicate Prevention
- Cannot add contact with same phone number as user's own number
- Cannot add contact with phone number that already exists for the user
- If setting as primary, automatically unsets other primary contacts

## Database Operations

### Tables Affected
- `sos_contacts`: Main table for storing emergency contacts
- `user_activity_logs`: Logs the contact addition activity

### Transaction Safety
- All operations are wrapped in database transactions
- Rollback on any error to maintain data consistency

## Usage Examples

### cURL Example
```bash
curl -X POST https://powderblue-pig-261057.hostingersite.com/mobile-api/sos/emergencycontactadd.php \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": 33,
    "name": "John Doe",
    "phone_number": "+1234567890",
    "relationship": "Family",
    "is_primary": true
  }'
```

### JavaScript Example
```javascript
const addEmergencyContact = async (contactData) => {
  try {
    const response = await fetch('/mobile-api/sos/emergencycontactadd.php', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(contactData)
    });
    
    const result = await response.json();
    
    if (result.success) {
      console.log('Contact added:', result.data.contact);
    } else {
      console.error('Error:', result.message);
    }
  } catch (error) {
    console.error('Network error:', error);
  }
};

// Usage
addEmergencyContact({
  user_id: 33,
  name: "Emergency Contact",
  phone_number: "+1234567890",
  relationship: "Family",
  is_primary: true
});
```

### PHP Example
```php
<?php
$data = [
    'user_id' => 33,
    'name' => 'Emergency Contact',
    'phone_number' => '+1234567890',
    'relationship' => 'Family',
    'is_primary' => true
];

$options = [
    'http' => [
        'header' => "Content-Type: application/json\r\n",
        'method' => 'POST',
        'content' => json_encode($data)
    ]
];

$context = stream_context_create($options);
$result = file_get_contents('https://powderblue-pig-261057.hostingersite.com/mobile-api/sos/emergencycontactadd.php', false, $context);
$response = json_decode($result, true);

if ($response['success']) {
    echo "Contact added successfully!";
} else {
    echo "Error: " . $response['message'];
}
?>
```

## Security Features

1. **Input Validation**: All inputs are validated and sanitized
2. **SQL Injection Prevention**: Uses prepared statements
3. **CORS Support**: Proper CORS headers for cross-origin requests
4. **Error Logging**: All errors are logged for debugging
5. **Transaction Safety**: Database operations are atomic

## Testing

Use the provided test file `test_emergency_contact_add.html` to test various scenarios:
- Valid contact addition
- Duplicate phone number prevention
- Own phone number prevention
- Invalid input validation

## Notes

- The API automatically handles setting/unsetting primary contacts
- All timestamps are in UTC
- The API logs all activities for audit purposes
- Phone numbers are stored exactly as provided (with or without + prefix)

