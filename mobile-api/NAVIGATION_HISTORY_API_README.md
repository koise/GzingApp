# Navigation History API Documentation

This document describes the Navigation History API endpoints for the GzingApp mobile application, which provides standalone navigation history tracking without dependency on navigation logs.

## Overview

The Navigation History API provides comprehensive tracking and management of successful user navigation sessions, including route data, timing information, fare calculations, and user ratings.

## Database Table

The API uses the `navigation_history` table with the following key fields:

- **Basic Info**: `id`, `user_id`
- **Location Data**: `start_latitude/longitude`, `end_latitude/longitude`
- **Route Info**: `destination_name`, `destination_address`, `route_distance`
- **Timing**: `estimated_duration`, `actual_duration`, `start_time`, `end_time`
- **Fare Info**: `estimated_fare`, `actual_fare`
- **Transport**: `transport_mode` (driving/walking/cycling/public_transport)
- **Quality**: `success_rate`, `route_quality`, `user_rating`, `is_favorite`
- **Traffic**: `traffic_condition`, `average_speed`
- **Metadata**: `waypoints_count`, `notes`, `completion_time`

## API Endpoints

### 1. Create Navigation History
**POST** `/mobile-api/navigation-history`

Creates a new navigation history entry for a successful navigation session.

#### Request Body
```json
{
  "user_id": 10,
  "start_latitude": 14.62270180,
  "start_longitude": 121.17656790,
  "end_latitude": 14.59758003,
  "end_longitude": 121.17244053,
  "destination_name": "Dela Paz Elementary School",
  "destination_address": "Antipolo City",
  "route_distance": 3.27,
  "estimated_duration": 6,
  "actual_duration": 8,
  "estimated_fare": 25.0,
  "actual_fare": 30.0,
  "transport_mode": "driving",
  "success_rate": 100.0,
  "completion_time": "2025-09-13 20:59:30",
  "start_time": "2025-09-13 20:59:14",
  "end_time": "2025-09-13 21:07:14",
  "waypoints_count": 5,
  "traffic_condition": "Moderate Traffic",
  "average_speed": 24.5,
  "route_quality": "good",
  "user_rating": 4,
  "notes": "Smooth navigation with moderate traffic",
  "is_favorite": 0
}
```

#### Required Fields
- `user_id`, `start_latitude`, `start_longitude`, `end_latitude`, `end_longitude`
- `destination_name`, `route_distance`, `start_time`, `end_time`, `transport_mode`

#### Response
```json
{
  "success": true,
  "message": "Navigation history created successfully",
  "data": {
    "id": 1,
    "user_id": 10,
    "start_latitude": "14.62270180",
    "start_longitude": "121.17656790",
    "end_latitude": "14.59758003",
    "end_longitude": "121.17244053",
    "destination_name": "Dela Paz Elementary School",
    "destination_address": "Antipolo City",
    "route_distance": "3.27",
    "estimated_duration": 6,
    "actual_duration": 8,
    "estimated_fare": "25.00",
    "actual_fare": "30.00",
    "transport_mode": "driving",
    "success_rate": "100.00",
    "completion_time": "2025-09-13 20:59:30",
    "start_time": "2025-09-13 20:59:14",
    "end_time": "2025-09-13 21:07:14",
    "waypoints_count": 5,
    "traffic_condition": "Moderate Traffic",
    "average_speed": "24.50",
    "route_quality": "good",
    "user_rating": 4,
    "notes": "Smooth navigation with moderate traffic",
    "is_favorite": 0,
    "created_at": "2025-09-13 20:59:14",
    "updated_at": "2025-09-13 21:07:14"
  }
}
```

### 2. Get All Navigation History
**GET** `/mobile-api/navigation-history?user_id={id}`

Retrieves all navigation history for a specific user with pagination support.

#### Query Parameters
- `user_id` (required): User ID to fetch history for
- `limit` (optional): Number of records to return (default: 50, max: 100)
- `offset` (optional): Number of records to skip (default: 0)
- `order_by` (optional): Field to order by (default: completion_time)
- `order_direction` (optional): ASC or DESC (default: DESC)

#### Example Request
```
GET /mobile-api/navigation-history?user_id=10&limit=20&offset=0&order_by=completion_time&order_direction=DESC
```

#### Response
```json
{
  "success": true,
  "message": "Navigation history retrieved successfully",
  "data": {
    "history": [
      {
        "id": 1,
        "user_id": 10,
        "start_latitude": "14.62270180",
        "start_longitude": "121.17656790",
        "end_latitude": "14.59758003",
        "end_longitude": "121.17244053",
        "destination_name": "Dela Paz Elementary School",
        "destination_address": "Antipolo City",
        "route_distance": "3.27",
        "estimated_duration": 6,
        "actual_duration": 8,
        "estimated_fare": "25.00",
        "actual_fare": "30.00",
        "transport_mode": "driving",
        "success_rate": "100.00",
        "completion_time": "2025-09-13 20:59:30",
        "start_time": "2025-09-13 20:59:14",
        "end_time": "2025-09-13 21:07:14",
        "waypoints_count": 5,
        "traffic_condition": "Moderate Traffic",
        "average_speed": "24.50",
        "route_quality": "good",
        "user_rating": 4,
        "notes": "Smooth navigation with moderate traffic",
        "is_favorite": 0,
        "created_at": "2025-09-13 20:59:14",
        "updated_at": "2025-09-13 21:07:14"
      }
    ],
    "pagination": {
      "total": 15,
      "limit": 20,
      "offset": 0,
      "has_more": false
    }
  }
}
```

### 3. Get Navigation History Stats
**GET** `/mobile-api/navigation-history/stats?user_id={id}`

Retrieves comprehensive statistics for a user's navigation history.

#### Query Parameters
- `user_id` (required): User ID to get stats for

#### Response
```json
{
  "success": true,
  "message": "Navigation history stats retrieved successfully",
  "data": {
    "user_id": 10,
    "total_navigations": 15,
    "successful_navigations": 14,
    "avg_duration_minutes": 12.5,
    "avg_distance_km": 2.8,
    "avg_speed_kmh": 18.2,
    "favorite_routes": 3,
    "last_navigation": "2025-09-13 21:15:00",
    "first_navigation": "2025-09-01 08:30:00",
    "popular_destinations": [
      {
        "destination_name": "Langhaya",
        "destination_address": "Antipolo City",
        "visit_count": 5,
        "avg_duration": 2.0,
        "avg_distance": 0.06,
        "last_visit": "2025-09-13 21:00:01",
        "avg_rating": 4.8
      }
    ],
    "transport_mode_stats": {
      "driving": 12,
      "walking": 2,
      "cycling": 1,
      "public_transport": 0
    },
    "route_quality_stats": {
      "excellent": 4,
      "good": 8,
      "fair": 2,
      "poor": 1
    }
  }
}
```

### 4. Get Navigation History by ID
**GET** `/mobile-api/navigation-history/{id}`

Retrieves a specific navigation history record by ID.

#### Path Parameters
- `id` (required): Navigation history ID

#### Query Parameters
- `user_id` (optional): User ID for validation

#### Response
```json
{
  "success": true,
  "message": "Navigation history retrieved successfully",
  "data": {
    "id": 1,
    "user_id": 10,
    "start_latitude": "14.62270180",
    "start_longitude": "121.17656790",
    "end_latitude": "14.59758003",
    "end_longitude": "121.17244053",
    "destination_name": "Dela Paz Elementary School",
    "destination_address": "Antipolo City",
    "route_distance": "3.27",
    "estimated_duration": 6,
    "actual_duration": 8,
    "estimated_fare": "25.00",
    "actual_fare": "30.00",
    "transport_mode": "driving",
    "success_rate": "100.00",
    "completion_time": "2025-09-13 20:59:30",
    "start_time": "2025-09-13 20:59:14",
    "end_time": "2025-09-13 21:07:14",
    "waypoints_count": 5,
    "traffic_condition": "Moderate Traffic",
    "average_speed": "24.50",
    "route_quality": "good",
    "user_rating": 4,
    "notes": "Smooth navigation with moderate traffic",
    "is_favorite": 0,
    "created_at": "2025-09-13 20:59:14",
    "updated_at": "2025-09-13 21:07:14"
  }
}
```

## Error Responses

All endpoints return consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2025-09-13 21:00:00"
}
```

## Database Setup

To set up the navigation history table, run the SQL script:

```sql
-- Run the navigation_history_standalone.sql file
-- This creates the table, indexes, and views
```

## Features

- ✅ **Standalone Operation**: No dependency on navigation logs
- ✅ **Comprehensive Tracking**: Route, timing, fare, and quality data
- ✅ **Pagination Support**: Efficient handling of large history datasets
- ✅ **Statistics**: Detailed analytics and popular destinations
- ✅ **Mock Responses**: Fallback when database is unavailable
- ✅ **Transaction Safety**: Database transactions for data integrity
- ✅ **Input Validation**: Comprehensive field validation
- ✅ **CORS Support**: Cross-origin request support

## Usage Examples

### Android Integration
```kotlin
// Create navigation history
val request = CreateNavigationHistoryRequest(
    userId = 10,
    startLatitude = 14.62270180,
    startLongitude = 121.17656790,
    endLatitude = 14.59758003,
    endLongitude = 121.17244053,
    destinationName = "Dela Paz Elementary School",
    destinationAddress = "Antipolo City",
    routeDistance = 3.27,
    estimatedDuration = 6,
    actualDuration = 8,
    estimatedFare = 25.0,
    actualFare = 30.0,
    transportMode = "driving",
    startTime = "2025-09-13 20:59:14",
    endTime = "2025-09-13 21:07:14",
    completionTime = "2025-09-13 20:59:30"
)

val response = apiService.createNavigationHistory(request)
```

### Get User History
```kotlin
// Get all navigation history for user
val response = apiService.getNavigationHistory(userId = 10, limit = 20, offset = 0)
```

### Get Statistics
```kotlin
// Get navigation statistics
val stats = apiService.getNavigationHistoryStats(userId = 10)
```

