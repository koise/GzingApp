package com.example.gzingapp.data

import com.google.gson.annotations.SerializedName

data class Landmark(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val coordinates: Coordinates,
    val address: String,
    val phone: String,
    @SerializedName("pin_color")
    val pinColor: String,
    @SerializedName("opening_time")
    val openingTime: String,
    @SerializedName("closing_time")
    val closingTime: String,
    @SerializedName("is_open")
    val isOpen: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("time_info")
    val timeInfo: TimeInfo,
    @SerializedName("created_at_formatted")
    val createdAtFormatted: String,
    @SerializedName("updated_at_formatted")
    val updatedAtFormatted: String
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class TimeInfo(
    @SerializedName("current_philippines_time")
    val currentPhilippinesTime: String,
    @SerializedName("opening_time")
    val openingTime: String,
    @SerializedName("closing_time")
    val closingTime: String,
    @SerializedName("calculated_status")
    val calculatedStatus: String
)

data class LandmarksResponse(
    val success: Boolean,
    val message: String,
    val data: LandmarksData,
    val timestamp: String
)

data class LandmarksData(
    val landmarks: List<Landmark>,
    val pagination: Pagination,
    val filters: Filters,
    @SerializedName("api_info")
    val apiInfo: ApiInfo
)

data class Pagination(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_items")
    val totalItems: Int,
    @SerializedName("items_per_page")
    val itemsPerPage: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
    @SerializedName("has_prev")
    val hasPrev: Boolean
)

data class Filters(
    val categories: List<String>
)

data class ApiInfo(
    val version: String,
    val endpoint: String,
    val description: String
)


