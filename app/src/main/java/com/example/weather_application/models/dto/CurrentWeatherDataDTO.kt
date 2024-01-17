package com.example.weather_application.models.dto

data class CurrentWeatherDataDTO(
    val name: String?,
    val main: MainDTO?,
    val weather: List<WeatherDTO>?
)

data class MainDTO(
    val temp: Double?
)

data class WeatherDTO(
    val description: String?,
    val id: Int?,
    val icon: String?
)
