package com.example.weather_application.models.repositories


interface WeatherRepository {
    suspend fun getCityCurrentWeather(cityName: String): WeatherInfoResponse
    suspend fun getCurrentWeatherInfo(long: String, lat: String): WeatherInfoResponse
}
