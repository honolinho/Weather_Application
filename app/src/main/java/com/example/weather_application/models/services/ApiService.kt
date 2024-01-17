package com.example.weather_application.models.services

import com.example.weather_application.constants.Constants.API_KEY
import com.example.weather_application.constants.Constants.METRIC
import com.example.weather_application.models.dto.CurrentWeatherDataDTO
import com.example.weather_application.models.dto.GeocodingDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("appId") appId: String = API_KEY,
        @Query("units") metric: String = METRIC
    ): Response<CurrentWeatherDataDTO>

    @GET("/data/2.5/weather")
    suspend fun getCurrentWeatherInfo(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appID") appId: String = API_KEY,
        @Query("units") metric: String = METRIC
    ): Response<CurrentWeatherDataDTO>

    @GET("/geo/1.0/direct")
    suspend fun getGeocoding(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = API_KEY,
        @Query("limit") limit: Int = 1
    ): Response<List<GeocodingDTO>>
}
