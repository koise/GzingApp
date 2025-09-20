# Mapbox Integration Setup

This document explains how to set up Mapbox v11 in your GzingApp project.

## Prerequisites

1. **Mapbox Account**: Sign up for a free account at [mapbox.com](https://www.mapbox.com/)
2. **Access Token**: Create an access token in your Mapbox account dashboard

## Setup Steps

### 1. Get Your Mapbox Access Token

1. Go to [Mapbox Account Dashboard](https://account.mapbox.com/)
2. Navigate to "Access tokens" section
3. Create a new token or use the default public token
4. Copy the token value

### 2. Configure the Access Token

1. Open `app/src/main/res/values/mapbox_config.xml`
2. Replace `YOUR_MAPBOX_ACCESS_TOKEN` with your actual token:

```xml
<string name="mapbox_access_token">pk.eyJ1IjoieW91cnVzZXJuYW1lIiwiYSI6ImNsZXhhbXBsZSJ9.your_actual_token_here</string>
```

### 3. Security Considerations

**For Development:**
- You can use a public token for testing
- The token is included in the APK, so don't use production tokens

**For Production:**
- Use restricted tokens with specific scopes
- Consider using build variants or environment variables
- Never commit real tokens to version control

### 4. Test the Integration

1. Build and run the app
2. Login to the app
3. Tap the map icon in the top bar
4. You should see a map centered on Manila, Philippines
5. Try switching between different map styles

## Features Included

### Core Mapbox SDKs
- **Maps SDK**: Basic map display and interaction
- **Navigation SDK**: For future navigation features
- **Search SDK**: For future location search features

### Map Styles Available
- Streets (default)
- Satellite
- Dark
- Light
- Outdoors
- Traffic Day/Night

### Default Configuration
- **Default Location**: Manila, Philippines (14.5995, 120.9842)
- **Default Zoom**: 12.0
- **Permissions**: Location access for future features

## Usage Examples

### Basic Map Display
```kotlin
@Composable
fun MyMap() {
    val mapViewportState = rememberMapViewportState {
        setCameraOptions(MapboxUtils.getDefaultCameraPosition())
    }
    
    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        style = MapboxUtils.getDefaultMapStyle()
    )
}
```

### Custom Map Style
```kotlin
val customStyle = fromStyleUri(Style.SATELLITE_STREETS)
```

### Location Permissions
```kotlin
if (MapboxUtils.hasLocationPermissions(context)) {
    // Enable location features
} else {
    // Request permissions
}
```

## Troubleshooting

### Common Issues

1. **Map not loading**: Check your access token
2. **Build errors**: Ensure all dependencies are properly added
3. **Permission denied**: Check location permissions in device settings

### Debug Steps

1. Check Android logs for Mapbox-related errors
2. Verify token is correctly set in `mapbox_config.xml`
3. Test with a simple map first before adding complex features

## Future Enhancements

The current setup provides a foundation for:
- Location tracking
- Navigation features
- Search functionality
- Custom markers and annotations
- Offline maps
- Real-time traffic data

## Resources

- [Mapbox Android SDK Documentation](https://docs.mapbox.com/android/maps/)
- [Mapbox Compose Integration](https://docs.mapbox.com/android/maps/guides/compose/)
- [Mapbox Navigation SDK](https://docs.mapbox.com/android/navigation/)
- [Mapbox Search SDK](https://docs.mapbox.com/android/search/)








