# Landmarks Mobile API

A mobile-optimized API for fetching landmarks data with advanced filtering and search capabilities.

## Overview

The Landmarks API provides access to active landmarks data optimized for mobile applications. It includes features like pagination, search, category filtering, and location-based filtering.

## Base URL

```
https://powderblue-pig-261057.hostingersite.com/mobile-api
```

## Endpoints

### GET /landmarks

Retrieve landmarks with optional filtering and pagination.

#### URL Parameters

| Parameter | Type | Description | Default | Example |
|-----------|------|-------------|---------|---------|
| `page` | integer | Page number (1-based) | 1 | `?page=2` |
| `limit` | integer | Items per page (max 100) | 50 | `?limit=10` |
| `search` | string | Search in name, description, address | - | `?search=cafe` |
| `category` | string | Filter by category | - | `?category=business` |
| `lat` | float | Latitude for location filtering | - | `?lat=14.6255` |
| `lng` | float | Longitude for location filtering | - | `?lng=121.1756` |
| `radius` | float | Radius in kilometers | - | `?radius=5` |

#### Example Requests

```bash
# Get all landmarks
GET /landmarks

# Get landmarks with pagination
GET /landmarks?page=1&limit=10

# Search for landmarks
GET /landmarks?search=cafe

# Filter by category
GET /landmarks?category=business

# Location-based filtering
GET /landmarks?lat=14.6255&lng=121.1756&radius=5

# Combined filters
GET /landmarks?search=restaurant&category=business&page=1&limit=5
```

#### Response Format

```json
{
  "success": true,
  "message": "Landmarks retrieved successfully",
  "data": {
    "landmarks": [
      {
        "id": 1,
        "name": "LRT Station",
        "description": "A Railway to Recto Station",
        "category": "transport",
        "coordinates": {
          "latitude": 14.62490919,
          "longitude": 121.12071524
        },
        "address": "Marcos Highway Mayamot, Antipolo City, Rizal, Philippines",
        "phone": "",
        "pin_color": "#3b82f6",
        "opening_time": "05:00:00",
        "closing_time": "21:00:00",
        "is_open": true,
        "created_at": "2025-09-14 14:10:45",
        "updated_at": "2025-09-14 14:10:45",
        "created_at_formatted": "Sep 14, 2025 2:10 PM",
        "updated_at_formatted": "Sep 14, 2025 2:10 PM"
      }
    ],
    "pagination": {
      "current_page": 1,
      "total_pages": 3,
      "total_items": 15,
      "items_per_page": 5,
      "has_next": true,
      "has_prev": false
    },
    "filters": {
      "categories": ["transport", "business", "school", "recreation", "residential"]
    },
    "api_info": {
      "version": "1.0.0",
      "endpoint": "landmarks",
      "description": "Mobile API for landmarks - active landmarks only"
    }
  },
  "timestamp": "2025-09-16 08:30:00"
}
```

## Features

### 🔍 Search Functionality
- Search across landmark name, description, and address
- Case-insensitive search
- Partial matching support

### 📂 Category Filtering
- Filter landmarks by category
- Available categories: transport, business, school, recreation, residential
- Case-sensitive exact matching

### 📍 Location-based Filtering
- Filter landmarks within a specified radius
- Uses Haversine formula for accurate distance calculation
- Requires latitude, longitude, and radius parameters

### 📄 Pagination
- Configurable page size (max 100 items per page)
- Page-based navigation
- Total count and page information included

### 🎯 Mobile Optimization
- Only active landmarks are returned
- Optimized data structure for mobile consumption
- Reduced payload size
- Coordinate format optimized for mobile maps

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "success": false,
  "message": "Invalid parameters",
  "data": null,
  "timestamp": "2025-09-16 08:30:00"
}
```

#### 404 Not Found
```json
{
  "success": false,
  "message": "Not found",
  "data": null,
  "timestamp": "2025-09-16 08:30:00"
}
```

#### 405 Method Not Allowed
```json
{
  "success": false,
  "message": "Method not allowed",
  "data": null,
  "timestamp": "2025-09-16 08:30:00"
}
```

#### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Internal server error",
  "data": null,
  "timestamp": "2025-09-16 08:30:00"
}
```

## Fallback Data

When the database is unavailable, the API automatically returns fallback sample data to ensure service continuity.

## Testing

### Automated Testing
Run the PHP test suite:
```bash
php test_landmarks_api.php
```

### Web-based Testing
Open `test_landmarks_web.html` in a web browser for interactive testing.

### Manual Testing Examples

```bash
# Test basic endpoint
curl "https://powderblue-pig-261057.hostingersite.com/mobile-api/landmarks"

# Test with pagination
curl "https://powderblue-pig-261057.hostingersite.com/mobile-api/landmarks?page=1&limit=5"

# Test search
curl "https://powderblue-pig-261057.hostingersite.com/mobile-api/landmarks?search=LRT"

# Test category filter
curl "https://powderblue-pig-261057.hostingersite.com/mobile-api/landmarks?category=transport"

# Test location filter
curl "https://powderblue-pig-261057.hostingersite.com/mobile-api/landmarks?lat=14.6255&lng=121.1756&radius=10"
```

## Data Structure

### Landmark Object
```json
{
  "id": 1,
  "name": "Landmark Name",
  "description": "Landmark description",
  "category": "business",
  "coordinates": {
    "latitude": 14.62490919,
    "longitude": 121.12071524
  },
  "address": "Full address",
  "phone": "Phone number",
  "pin_color": "#3b82f6",
  "opening_time": "08:00:00",
  "closing_time": "22:00:00",
  "is_open": true,
  "created_at": "2025-09-14 14:10:45",
  "updated_at": "2025-09-14 14:10:45",
  "created_at_formatted": "Sep 14, 2025 2:10 PM",
  "updated_at_formatted": "Sep 14, 2025 2:10 PM"
}
```

### Pagination Object
```json
{
  "current_page": 1,
  "total_pages": 3,
  "total_items": 15,
  "items_per_page": 5,
  "has_next": true,
  "has_prev": false
}
```

## Security

- Only active landmarks are accessible
- Input validation and sanitization
- SQL injection protection
- CORS headers configured for mobile access

## Performance

- Optimized database queries
- Pagination to limit data transfer
- Efficient location-based filtering
- Fallback data for reliability

## Version History

- **v1.0.0** - Initial release with basic CRUD operations and mobile optimization

## Support

For issues or questions, please refer to the main API documentation or contact the development team.
