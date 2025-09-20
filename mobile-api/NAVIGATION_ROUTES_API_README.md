# Navigation Routes API Documentation

This document describes the Navigation Routes API for the GzingApp mobile application, which allows users to save and manage their favorite navigation routes for future reference.

## Overview

The Navigation Routes API provides functionality to save successful navigation sessions as reusable routes, allowing users to quickly access their frequently used destinations and share routes with others.

## Database Table

The API uses the `navigation_routes` table with the following key fields:

- **Basic Info**: `id`, `user_id`, `route_name`, `route_description`
- **Location Data**: `start_latitude/longitude`, `end_latitude/longitude`
- **Route Info**: `destination_name`, `destination_address`, `route_distance`
- **Timing**: `estimated_duration`, `average_speed`
- **Transport**: `transport_mode` (driving/walking/cycling/public_transport)
- **Quality**: `route_quality` (excellent/good/fair/poor), `traffic_condition`
- **Usage**: `usage_count`, `last_used`, `is_favorite`, `is_public`
- **Coordinates**: `route_coordinates` (JSON array of route points)
- **Metadata**: `waypoints_count`, `created_at`, `updated_at`

## API Endpoints

### 1. Create Navigation Route
**POST** `/mobile-api/endpoints/navigation-routes/create_navigation_route.php`

Creates a new saved navigation route from a successful navigation session.

#### Request Body
```json
{
  "user_id": 10,
  "route_name": "Home to Work",
  "route_description": "Daily commute route to office",
  "start_latitude": 14.62270180,
  "start_longitude": 121.17656790,
  "end_latitude": 14.59758003,
  "end_longitude": 121.17244053,
  "destination_name": "Office Building",
  "destination_address": "Makati City",
  "route_distance": 3.27,
  "estimated_duration": 15,
  "transport_mode": "driving",
  "route_quality": "good",
  "traffic_condition": "Moderate Traffic",
  "average_speed": 24.5,
  "waypoints_count": 5,
  "route_coordinates": [
    {"lat": 14.62270180, "lng": 121.17656790},
    {"lat": 14.59758003, "lng": 121.17244053}
  ],
  "is_favorite": 1,
  "is_public": 0
}
```

#### Required Fields
- `user_id`, `route_name`, `start_latitude`, `start_longitude`
- `end_latitude`, `end_longitude`, `destination_name`, `route_distance`, `transport_mode`

#### Response
```json
{
  "success": true,
  "message": "Navigation route created successfully",
  "data": {
    "id": 1,
    "user_id": 10,
    "route_name": "Home to Work",
    "route_description": "Daily commute route to office",
    "start_latitude": "14.62270180",
    "start_longitude": "121.17656790",
    "end_latitude": "14.59758003",
    "end_longitude": "121.17244053",
    "destination_name": "Office Building",
    "destination_address": "Makati City",
    "route_distance": "3.27",
    "estimated_duration": 15,
    "transport_mode": "driving",
    "route_quality": "good",
    "traffic_condition": "Moderate Traffic",
    "average_speed": "24.50",
    "waypoints_count": 5,
    "route_coordinates": [
      {"lat": 14.62270180, "lng": 121.17656790},
      {"lat": 14.59758003, "lng": 121.17244053}
    ],
    "is_favorite": 1,
    "is_public": 0,
    "usage_count": 0,
    "last_used": null,
    "created_at": "2025-01-15 10:00:00",
    "updated_at": "2025-01-15 10:00:00"
  }
}
```

### 2. Get Navigation Routes
**GET** `/mobile-api/endpoints/navigation-routes/get_navigation_routes.php`

Retrieves user's saved navigation routes with pagination and filtering support.

#### Query Parameters
- `user_id` (required): User ID to fetch routes for
- `limit` (optional): Number of records to return (default: 50, max: 100)
- `offset` (optional): Number of records to skip (default: 0)
- `order_by` (optional): Field to order by (created_at, updated_at, route_name, destination_name, usage_count, last_used)
- `order_direction` (optional): ASC or DESC (default: DESC)
- `favorites_only` (optional): Show only favorite routes (true/false)
- `search` (optional): Search in route name, destination name, or address

#### Example Request
```
GET /mobile-api/endpoints/navigation-routes/get_navigation_routes.php?user_id=10&limit=20&favorites_only=true&search=office
```

#### Response
```json
{
  "success": true,
  "message": "Navigation routes retrieved successfully",
  "data": {
    "routes": [
      {
        "id": 1,
        "user_id": 10,
        "route_name": "Home to Work",
        "route_description": "Daily commute route to office",
        "start_latitude": "14.62270180",
        "start_longitude": "121.17656790",
        "end_latitude": "14.59758003",
        "end_longitude": "121.17244053",
        "destination_name": "Office Building",
        "destination_address": "Makati City",
        "route_distance": "3.27",
        "estimated_duration": 15,
        "transport_mode": "driving",
        "route_quality": "good",
        "traffic_condition": "Moderate Traffic",
        "average_speed": "24.50",
        "waypoints_count": 5,
        "route_coordinates": [
          {"lat": 14.62270180, "lng": 121.17656790},
          {"lat": 14.59758003, "lng": 121.17244053}
        ],
        "is_favorite": 1,
        "is_public": 0,
        "usage_count": 25,
        "last_used": "2025-01-15 08:30:00",
        "created_at": "2025-01-01 10:00:00",
        "updated_at": "2025-01-15 08:30:00"
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

## Android Integration

### MapActivity Integration

The navigation routes feature is integrated into the MapActivity to automatically prompt users to save routes after successful navigation:

1. **Automatic Dialog**: After reaching destination and stopping the alarm, a dialog appears asking if the user wants to save the route
2. **Route Information**: Pre-fills route name with destination name
3. **User Input**: Allows users to add description, rating, and mark as favorite
4. **Save Process**: Creates route entry via API call

### Key Components

#### Data Classes
- `CreateNavigationRouteRequest`: Request payload for creating routes
- `NavigationRoute`: Route data model
- `NavigationRoutesData`: Response wrapper with pagination

#### Repository
- `NavigationRouteRepository`: Handles API calls and data management
- Methods: `createNavigationRoute()`, `getNavigationRoutes()`, `getFavoriteRoutes()`, `searchRoutes()`

#### API Service
- `NavigationRouteApiService`: Retrofit interface for API endpoints
- Endpoints: Create route, Get routes with filtering

### Usage Example

```kotlin
// Create navigation route
val request = CreateNavigationRouteRequest(
    userId = 10,
    routeName = "Home to Work",
    routeDescription = "Daily commute route",
    startLatitude = 14.62270180,
    startLongitude = 121.17656790,
    endLatitude = 14.59758003,
    endLongitude = 121.17244053,
    destinationName = "Office Building",
    destinationAddress = "Makati City",
    routeDistance = 3.27,
    estimatedDuration = 15,
    transportMode = "driving",
    routeQuality = "good",
    isFavorite = 1
)

val result = navigationRouteRepository.createNavigationRoute(request)
result.onSuccess { response ->
    // Route saved successfully
    Toast.makeText(context, "Route saved!", Toast.LENGTH_SHORT).show()
}.onFailure { error ->
    // Handle error
    Log.e("SaveRoute", "Failed to save route", error)
}
```

## Database Features

### Views
- `navigation_routes_stats`: User statistics for saved routes
- `popular_public_routes`: Most used public routes
- `user_most_used_routes`: User's most frequently used routes

### Stored Procedures
- `IncrementRouteUsage(route_id)`: Increment usage count when route is used
- `GetUserFavoriteRoutes(user_id)`: Get user's favorite routes
- `SearchRoutesByDestination(user_id, search_term)`: Search routes by destination

### Triggers
- `update_last_used_on_usage`: Automatically updates last_used timestamp when usage_count changes

## Error Responses

All endpoints return consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2025-01-15 10:00:00"
}
```

## Database Setup

To set up the navigation routes table, run the SQL script:

```sql
-- Run the navigation_routes.sql file
-- This creates the table, indexes, views, and stored procedures
```

## Features

- ✅ **Route Saving**: Save successful navigation sessions as reusable routes
- ✅ **User Rating**: Rate routes for quality assessment
- ✅ **Favorites**: Mark frequently used routes as favorites
- ✅ **Search**: Search routes by name, destination, or address
- ✅ **Pagination**: Efficient handling of large route collections
- ✅ **Usage Tracking**: Track how often routes are used
- ✅ **Public Sharing**: Option to share routes publicly
- ✅ **Statistics**: Comprehensive route usage analytics
- ✅ **Mock Responses**: Fallback when database is unavailable
- ✅ **Transaction Safety**: Database transactions for data integrity
- ✅ **Input Validation**: Comprehensive field validation
- ✅ **CORS Support**: Cross-origin request support

## Integration with MapActivity

The navigation routes feature is seamlessly integrated into the MapActivity:

1. **Automatic Trigger**: Dialog appears after successful navigation completion
2. **Smart Defaults**: Pre-fills route name with destination name
3. **User Experience**: Simple dialog with rating and favorite options
4. **Error Handling**: Graceful handling of save failures
5. **Feedback**: Toast messages for success/failure states

This implementation provides users with a convenient way to save and reuse their successful navigation routes, enhancing the overall navigation experience in the GzingApp.
