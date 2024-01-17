package com.example.weather_application.models.dto

data class GeocodingDTO(
    val country: String?,
    val lat: Double?,
    val lon: Double?,
    val name: String?,
    val state: String?
)
