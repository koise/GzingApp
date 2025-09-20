# Navigation API Documentation

This document describes the Navigation API endpoints for the GzingApp mobile application, which uses the new `user_navigation_logs` database structure.

## Overview

The Navigation API provides comprehensive tracking and management of user navigation activities, including route data, traffic information, waypoints, and navigation events.

## Database Tables

The API uses the following database tables:

- **`user_navigation_logs`** - Main navigation log table
- **`navigation_route_instructions`** - Turn-by-turn route instructions
- **`navigation_waypoints`** - Route waypoints and stops
- **`navigation_route_polylines`** - Route geometry data for map visualization
- **`navigation_traffic_data`** - Traffic conditions and delays
- **`navigation_events`** - Navigation events and milestones

## API Endpoints

### 1. Create Navigation Log
**POST** `/navigation`

Creates a new navigation log entry.

#### Request Body
```json
{
  "activity_type": "navigation_start",
  "start_latitude": 14.5995,
  "start_longitude": 120.9842,
  "destination_name": "Mall of Asia",
  "destination_address": "Seaside Blvd, Pasay, Metro Manila",
  "route_distance": 15.5,
  "estimated_duration": 25,
  "transport_mode": "driving",
  "device_model": "Samsung Galaxy S21",
  "device_id": "test_device_123",
  "battery_level": 85,
  "network_type": "WiFi",
  "gps_accuracy": "high",
  "screen_resolution": "1080x2400",
  "available_storage": 50000000000,
  "app_version": "1.0.0",
  "os_version": "Android 12",
  "additional_data": {
    "traffic_enabled": true,
    "route_alternatives": 3
  },
  "route_instructions": [
    {
      "instruction": "Head north on Roxas Blvd",
      "distance": 500,
      "duration": 60,
      "maneuver": "straight"
    }
  ],
  "waypoints": [
    {
      "lat": 14.5995,
      "lng": 120.9842,
      "name": "Starting Point",
      "type": "start"
    }
  ],
  "route_polylines": [
    {
      "type": "api_response",
      "data": "encoded_polyline_data",
      "color": "#D2B48C",
      "width": 6.0,
      "opacity": 0.8
    }
  ],
  "traffic_data": {
    "condition": "Moderate Traffic",
    "average_speed": 25.5,
    "delay": 5,
    "enabled": true
  },
  "navigation_events": [
    {
      "type": "navigation_start",
      "data": {"started_at": "2025-01-12 10:30:00"},
      "latitude": 14.5995,
      "longitude": 120.9842
    }
  ]
}
```

#### Response
```json
{
  "success": true,
  "message": "Navigation log created successfully",
  "data": {
    "log": { ... },
    "log_id": 123
  },
  "timestamp": "2025-01-12 10:30:00"
}
```

### 2. Get Navigation Logs
**GET** `/navigation`

Retrieves navigation logs with filtering and pagination.

#### Query Parameters
- `page` (optional) - Page number (default: 1)
- `limit` (optional) - Items per page (default: 20, max: 100)
- `activity_type` (optional) - Filter by activity type
- `transport_mode` (optional) - Filter by transport mode
- `destination_reached` (optional) - Filter by destination reached status
- `date_from` (optional) - Filter from date (YYYY-MM-DD)
- `date_to` (optional) - Filter to date (YYYY-MM-DD)
- `include_details` (optional) - Include detailed route data (default: false)

#### Example Request
```
GET /navigation?page=1&limit=10&include_details=true&activity_type=navigation_start
```

#### Response
```json
{
  "success": true,
  "message": "Navigation logs retrieved successfully",
  "data": {
    "logs": [
      {
        "id": 123,
        "user_id": 1,
        "activity_type": "navigation_start",
        "destination_name": "Mall of Asia",
        "route_distance": 15.5,
        "estimated_duration": 25,
        "transport_mode": "driving",
        "created_at": "2025-01-12 10:30:00",
        "route_instructions": [...],
        "waypoints": [...],
        "route_polylines": [...],
        "traffic_data": [...],
        "navigation_events": [...]
      }
    ],
    "pagination": {
      "current_page": 1,
      "total_pages": 5,
      "total_count": 50,
      "limit": 10,
      "has_next_page": true,
      "has_prev_page": false
    }
  }
}
```

### 3. Get Navigation Statistics
**GET** `/navigation/stats`

Retrieves navigation statistics for the authenticated user.

#### Query Parameters
- `period` (optional) - Time period (all, week, month, year)
- `include_destinations` (optional) - Include popular destinations (default: true)
- `include_transport_modes` (optional) - Include transport mode stats (default: true)

#### Example Request
```
GET /navigation/stats?period=month&include_destinations=true
```

#### Response
```json
{
  "success": true,
  "message": "Navigation statistics retrieved successfully",
  "data": {
    "period": "month",
    "basic_stats": {
      "total_navigations": 25,
      "navigation_starts": 25,
      "destinations_reached": 22,
      "success_rate": 88.0,
      "avg_navigation_duration": 28.5,
      "avg_route_distance": 12.3,
      "total_distance_traveled": 307.5,
      "transport_modes_used": 3,
      "active_days": 15
    },
    "popular_destinations": [
      {
        "destination_name": "Mall of Asia",
        "visit_count": 5,
        "avg_distance": 15.2,
        "avg_duration": 25.0
      }
    ],
    "transport_mode_stats": [
      {
        "transport_mode": "driving",
        "usage_count": 20,
        "avg_distance": 15.0,
        "avg_duration": 30.0
      }
    ],
    "recent_activity": [...],
    "traffic_stats": [...],
    "time_stats": [...]
  }
}
```

### 4. Get Navigation Log Detail
**GET** `/navigation/{log_id}`

Retrieves detailed information for a specific navigation log.

#### Example Request
```
GET /navigation/123
```

#### Response
```json
{
  "success": true,
  "message": "Navigation log details retrieved successfully",
  "data": {
    "log": { ... },
    "route_instructions": [...],
    "waypoints": [...],
    "route_polylines": [...],
    "traffic_data": [...],
    "navigation_events": [...],
    "navigation_metrics": {
      "duration_accuracy": 95.2,
      "distance_accuracy": 98.1,
      "success_rate": 100,
      "efficiency_score": 97.8
    },
    "summary": {
      "total_instructions": 15,
      "total_waypoints": 3,
      "total_polylines": 2,
      "total_events": 8,
      "has_traffic_data": true,
      "navigation_completed": true,
      "destination_reached": true
    }
  }
}
```

### 5. Update Navigation Log
**PUT** `/navigation/{log_id}`

Updates an existing navigation log.

#### Request Body
```json
{
  "activity_type": "navigation_stop",
  "end_latitude": 14.5356,
  "end_longitude": 120.9821,
  "destination_reached": true,
  "actual_duration": 28,
  "stop_reason": "destination_reached",
  "additional_data": {
    "actual_distance": 15.2,
    "completion_time": "2025-01-12 11:00:00"
  },
  "navigation_events": [
    {
      "type": "destination_reached",
      "data": {"arrived_at": "2025-01-12 11:00:00"},
      "latitude": 14.5356,
      "longitude": 120.9821
    }
  ]
}
```

#### Response
```json
{
  "success": true,
  "message": "Navigation log updated successfully",
  "data": {
    "log": { ... },
    "updated_fields": ["activity_type", "end_latitude", "destination_reached"]
  }
}
```

### 6. Stop Navigation
**POST** `/navigation/stop`

Stops an active navigation session.

#### Request Body
```json
{
  "log_id": 123,
  "end_latitude": 14.5356,
  "end_longitude": 120.9821,
  "destination_reached": true,
  "navigation_duration": 28,
  "actual_distance": 15.2,
  "stop_reason": "destination_reached",
  "additional_data": {
    "completion_time": "2025-01-12 11:00:00",
    "user_rating": 5
  }
}
```

#### Response
```json
{
  "success": true,
  "message": "Navigation stopped successfully",
  "data": {
    "log": { ... },
    "navigation_summary": {
      "log_id": 123,
      "start_time": "2025-01-12 10:30:00",
      "end_time": "2025-01-12 11:00:00",
      "duration_minutes": 28,
      "destination_reached": true,
      "stop_reason": "destination_reached"
    }
  }
}
```

## Data Models

### Activity Types
- `navigation_start` - Navigation session started
- `navigation_stop` - Navigation session stopped
- `navigation_pause` - Navigation paused
- `navigation_resume` - Navigation resumed
- `route_change` - Route changed during navigation
- `destination_reached` - Destination reached

### Transport Modes
- `driving` - Car/vehicle navigation
- `walking` - Walking navigation
- `cycling` - Bicycle navigation
- `transit` - Public transportation
- `car` - Alias for driving
- `walk` - Alias for walking
- `motor` - Motorcycle navigation

### Network Types
- `WiFi` - WiFi connection
- `Mobile` - Mobile data connection
- `Unknown` - Unknown network type

### Traffic Conditions
- `Heavy Traffic` - Heavy traffic conditions
- `Moderate Traffic` - Moderate traffic conditions
- `No Traffic` - No traffic delays
- `Unknown` - Unknown traffic conditions

### Event Types
- `waypoint_reached` - Waypoint reached
- `route_deviation` - Route deviation detected
- `voice_announcement` - Voice announcement made
- `notification_sent` - Notification sent
- `alarm_triggered` - Alarm triggered
- `drawer_disabled` - Drawer disabled during navigation
- `bottom_nav_hidden` - Bottom navigation hidden
- `location_update` - Location updated
- `geofence_entered` - Geofence entered
- `geofence_exited` - Geofence exited

## Error Handling

All endpoints return consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2025-01-12 10:30:00"
}
```

### Common HTTP Status Codes
- `200` - Success
- `400` - Bad Request (invalid input)
- `401` - Unauthorized (authentication required)
- `404` - Not Found (log not found)
- `422` - Validation Error
- `500` - Internal Server Error

## Authentication

All endpoints require authentication via session management. The API uses the existing session system from the mobile-api.

## Testing

Use the provided test file `test_navigation_api.php` to test all endpoints:

```bash
php test_navigation_api.php
```

## Integration with Android App

The Android app can integrate with this API using Retrofit:

```kotlin
// Example Retrofit interface
interface NavigationApiService {
    @POST("navigation")
    suspend fun createNavigationLog(@Body log: CreateNavigationLogRequest): Response<ApiResponse<NavigationLog>>
    
    @GET("navigation")
    suspend fun getNavigationLogs(@QueryMap params: Map<String, String>): Response<ApiResponse<NavigationLogsResponse>>
    
    @GET("navigation/stats")
    suspend fun getNavigationStats(@QueryMap params: Map<String, String>): Response<ApiResponse<NavigationStatsResponse>>
    
    @GET("navigation/{logId}")
    suspend fun getNavigationLogDetail(@Path("logId") logId: Int): Response<ApiResponse<NavigationLogDetailResponse>>
    
    @PUT("navigation/{logId}")
    suspend fun updateNavigationLog(@Path("logId") logId: Int, @Body update: UpdateNavigationLogRequest): Response<ApiResponse<NavigationLog>>
    
    @POST("navigation/stop")
    suspend fun stopNavigation(@Body stop: StopNavigationRequest): Response<ApiResponse<StopNavigationResponse>>
}
```

## Database Setup

Before using the API, ensure the database tables are created by running the SQL script:

```sql
-- Run the user_routes_navigation.sql script
source user_routes_navigation.sql
```

## Features

- ✅ Complete navigation tracking
- ✅ Route instructions and waypoints
- ✅ Traffic data integration
- ✅ Navigation events logging
- ✅ Statistics and analytics
- ✅ Pagination and filtering
- ✅ JSON data support
- ✅ Transaction safety
- ✅ Input validation
- ✅ Error handling
- ✅ Session-based authentication
- ✅ CORS support
- ✅ Comprehensive logging

