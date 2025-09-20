package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

// Route data models
data class Route(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("pincount")
    val pinCount: Int,
    
    @SerializedName("kilometer")
    val kilometer: String, // API returns as string
    
    @SerializedName("estimated_total_fare")
    val estimatedTotalFare: String, // API returns as string
    
    @SerializedName("map_details")
    val mapDetails: MapDetails? = null,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class MapDetails(
    @SerializedName("pins")
    val pins: List<Pin>? = null,
    
    @SerializedName("center")
    val center: Center? = null,
    
    @SerializedName("zoom")
    val zoom: Double? = null,
    
    @SerializedName("fifo_order")
    val fifoOrder: List<String>? = null,
    
    @SerializedName("route_line")
    val routeLine: RouteLine? = null
)

data class Pin(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("number")
    val number: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("coordinates")
    val coordinates: List<Double>? = null, // [lng, lat]
    
    @SerializedName("lng")
    val lng: Double,
    
    @SerializedName("lat")
    val lat: Double,
    
    @SerializedName("addedAt")
    val addedAt: String? = null,
    
    @SerializedName("placeName")
    val placeName: String? = null,
    
    @SerializedName("address")
    val address: String? = null
)

data class Center(
    @SerializedName("lng")
    val lng: Double,
    
    @SerializedName("lat")
    val lat: Double
)

data class RouteLine(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("properties")
    val properties: List<Any>? = null,
    
    @SerializedName("geometry")
    val geometry: RouteGeometry? = null
)

data class RouteGeometry(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("properties")
    val properties: List<Any>? = null,
    
    @SerializedName("geometry")
    val geometry: GeometryData? = null
)

data class GeometryData(
    @SerializedName("coordinates")
    val coordinates: List<List<Double>>? = null, // [[lng, lat], [lng, lat], ...]
    
    @SerializedName("type")
    val type: String
)


// API Response models
data class RoutesResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<Route>? = null
)

data class RoutesApiResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: RoutesData? = null
)

data class RoutesData(
    @SerializedName("routes")
    val routes: List<Route>? = null,
    
    @SerializedName("pagination")
    val pagination: com.example.gzingapp.data.PaginationInfo? = null
)

data class RouteDetailsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: RouteDetailsData? = null
)

// Create Route Request and Response models
data class CreateRouteRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("pincount")
    val pincount: Int = 0,
    
    @SerializedName("kilometer")
    val kilometer: Double = 0.0,
    
    @SerializedName("estimated_total_fare")
    val estimated_total_fare: Double = 0.0,
    
    @SerializedName("map_details")
    val map_details: MapDetails? = null,
    
    @SerializedName("status")
    val status: String = "active"
)

data class CreateRouteResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: CreateRouteData? = null
)

data class CreateRouteData(
    @SerializedName("route")
    val route: Route? = null
)

