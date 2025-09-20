# Emergency SMS Service API

## Overview
The Emergency SMS Service provides a robust API for sending emergency SMS messages with location information and reverse geocoding. This service is designed for emergency situations where users need to quickly notify contacts about their location and situation.

## Features
- 🚨 **Emergency SMS**: Send SMS with current location and geocoding
- 📍 **Reverse Geocoding**: Automatic address lookup using OpenStreetMap
- 🗺️ **Google Maps Integration**: Includes direct Google Maps links
- 📱 **Philippine SMS Support**: Optimized for Philippine phone numbers
- 📊 **Logging**: Complete audit trail of all SMS attempts
- 🔄 **Batch Sending**: Send to multiple contacts simultaneously

## API Endpoint

### Send Emergency SMS
**POST** `/mobile-api/endpoints/sms/send_emergency_sms`

#### Request Body
```json
{
    "user_id": 10,
    "latitude": 14.62270180,
    "longitude": 121.17656790,
    "emergency_type": "emergency",
    "message": "Something went wrong, I need immediate help!",
    "contacts": [
        "09123456789",
        "63987654321",
        "+639123456789"
    ]
}
```

#### Parameters
- `user_id` (required): User ID
- `latitude` (required): Current latitude (-90 to 90)
- `longitude` (required): Current longitude (-180 to 180)
- `emergency_type` (optional): Type of emergency (default: "emergency")
- `message` (optional): Custom message to include
- `contacts` (required): Array of phone numbers

#### Response
```json
{
    "success": true,
    "message": "Emergency SMS sent to 2 out of 3 contacts",
    "data": {
        "total_contacts": 3,
        "successful_sends": 2,
        "failed_sends": 1,
        "location_info": {
            "address": "Antipolo City, Rizal, Philippines",
            "formatted_address": "Antipolo City, Rizal, Philippines",
            "place_type": "city",
            "confidence": 0.8
        },
        "results": [
            {
                "phone": "639123456789",
                "success": true,
                "message": "SMS sent successfully"
            },
            {
                "phone": "63987654321",
                "success": true,
                "message": "SMS sent successfully"
            },
            {
                "phone": "639123456789",
                "success": false,
                "message": "Invalid phone number format"
            }
        ]
    }
}
```

## SMS Message Format

The generated SMS message includes:
- 🚨 Emergency alert header
- 📍 Detailed location information
- 🗺️ Google Maps link for easy navigation
- ⏰ Timestamp of the emergency
- 🚨 Emergency type classification
- 📱 Source identification (GzingApp)

### Example SMS Message:
```
🚨 EMERGENCY ALERT 🚨

Message: Something went wrong, I need immediate help!

📍 Location Details:
Address: Antipolo City, Rizal, Philippines
Coordinates: 14.62270180, 121.17656790
Google Maps: https://maps.google.com/maps?q=14.62270180,121.17656790

⏰ Time: 2025-01-14 10:30:00
🚨 Emergency Type: Emergency

Please help immediately! This is an automated emergency message from GzingApp.
```

## Phone Number Formats

The service supports various Philippine phone number formats:
- `09123456789` (10 digits starting with 9)
- `639123456789` (12 digits with country code)
- `+639123456789` (13 digits with + and country code)
- `09123456789` (11 digits starting with 09)

All formats are automatically converted to the international format `639123456789`.

## Emergency Types

Supported emergency types:
- `emergency` - General emergency
- `medical` - Medical emergency
- `security` - Security/safety emergency
- `fire` - Fire emergency
- `police` - Police emergency

## Database Schema

### emergency_sms_logs Table
```sql
CREATE TABLE `emergency_sms_logs` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_id` int(11) NOT NULL,
    `latitude` decimal(10,8) NOT NULL,
    `longitude` decimal(11,8) NOT NULL,
    `emergency_type` varchar(50) DEFAULT 'emergency',
    `contacts_json` text NOT NULL,
    `success_count` int(11) DEFAULT 0,
    `failure_count` int(11) DEFAULT 0,
    `message_content` text,
    `location_address` text,
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_emergency_type` (`emergency_type`)
);
```

## Configuration

### SMS Service Settings
- **API Token**: `dO1YvHSMoFyHSV8FKoYl6kAx0ndKO8ToaxhgdRDE`
- **API URL**: `https://api.philsms.com/send`
- **Sender ID**: `GzingApp`

### Geocoding Service
- **Provider**: OpenStreetMap Nominatim API
- **Rate Limit**: 1 request per second
- **Timeout**: 10 seconds

## Testing

### Test Files
1. `test_emergency_sms.php` - Comprehensive test with multiple contacts
2. `test_sms_simple.php` - Simple test with single contact

### Running Tests
```bash
# Simple test
php test_sms_simple.php

# Comprehensive test
php test_emergency_sms.php
```

## Error Handling

The API handles various error scenarios:
- Invalid coordinates
- Invalid phone numbers
- SMS service failures
- Geocoding service failures
- Network timeouts

All errors are logged and returned with appropriate error messages.

## Security Considerations

- Input validation for all parameters
- Phone number sanitization
- Rate limiting (implemented at SMS service level)
- Audit logging for all emergency SMS attempts
- No sensitive data exposure in error messages

## Integration with Android App

The service integrates with the Android app through:
- `EmergencySMSService.kt` - Service class for SMS operations
- `EmergencySMSModels.kt` - Data models for requests/responses
- `ApiService.kt` - Retrofit interface for API calls

### Usage Example (Android)
```kotlin
val smsService = EmergencySMSService(context)
val result = smsService.sendEmergencySMSWithDefaultMessage(
    latitude = 14.62270180,
    longitude = 121.17656790,
    contacts = listOf("09123456789", "63987654321")
)

result.onSuccess { response ->
    // Handle success
    Log.d("SMS", "Sent to ${response.data?.successfulSends} contacts")
}.onFailure { error ->
    // Handle error
    Log.e("SMS", "Failed to send SMS", error)
}
```

## Monitoring and Analytics

The service provides comprehensive logging for:
- SMS delivery success/failure rates
- Geographic distribution of emergencies
- Emergency type analysis
- Response time metrics
- User behavior patterns

## Support

For issues or questions regarding the Emergency SMS Service:
1. Check the test files for usage examples
2. Review the error logs in the database
3. Verify SMS service credentials and quotas
4. Test with the provided test scripts

## Changelog

### Version 1.0.0
- Initial release
- Basic emergency SMS functionality
- Reverse geocoding integration
- Philippine phone number support
- Comprehensive logging
- Android integration

