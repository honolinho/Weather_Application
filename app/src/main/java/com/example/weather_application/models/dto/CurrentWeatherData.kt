package com.example.weather_application.models.dto

data class CurrentWeatherData(
    val name: String,
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Double
)

data class Weather(
    val description: String,
    val id: Int,
    val icon: String
)


