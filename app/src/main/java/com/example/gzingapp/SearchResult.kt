package com.example.gzingapp

import com.mapbox.geojson.Point

data class SearchResult(
    val name: String,
    val address: String,
    val coordinate: Point,
    val category: String
)
