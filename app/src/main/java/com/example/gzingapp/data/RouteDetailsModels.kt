package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

// Separate data models for route details API response (used by RoutesMapsActivity)
// These models use camelCase field names to match the route details API response

data class RouteDetails(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("pinCount")
    val pinCount: Int,
    
    @SerializedName("kilometer")
    val kilometer: Double,
    
    @SerializedName("estimatedTotalFare")
    val estimatedTotalFare: Double,
    
    @SerializedName("mapDetails")
    val mapDetails: RouteDetailsMapDetails? = null,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class RouteDetailsMapDetails(
    @SerializedName("pins")
    val pins: List<RouteDetailsPin>? = null,
    
    @SerializedName("center")
    val center: RouteDetailsCenter? = null,
    
    @SerializedName("zoom")
    val zoom: Double? = null,
    
    @SerializedName("fifo_order")
    val fifoOrder: List<String>? = null,
    
    @SerializedName("routeLine")
    val routeLine: RouteDetailsRouteLine? = null
)

data class RouteDetailsPin(
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

data class RouteDetailsCenter(
    @SerializedName("lng")
    val lng: Double,
    
    @SerializedName("lat")
    val lat: Double
)

data class RouteDetailsRouteLine(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("properties")
    val properties: List<Any>? = null,
    
    @SerializedName("geometry")
    val geometry: RouteDetailsRouteGeometry? = null
)

data class RouteDetailsRouteGeometry(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("properties")
    val properties: List<Any>? = null,
    
    @SerializedName("geometry")
    val geometry: RouteDetailsGeometryData? = null
)

data class RouteDetailsGeometryData(
    @SerializedName("coordinates")
    val coordinates: List<List<Double>>? = null, // [[lng, lat], [lng, lat], ...]
    
    @SerializedName("type")
    val type: String
)

// API Response models for route details
data class RouteDetailsApiResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: RouteDetailsData? = null
)

data class RouteDetailsData(
    @SerializedName("route")
    val route: RouteDetails? = null
)


