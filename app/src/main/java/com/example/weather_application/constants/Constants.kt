package com.example.weather_application.constants

import com.example.weather_application.BuildConfig

object Constants {
    const val BASE_URL = "https://api.openweathermap.org"
    /**
     * Make sure you've added API_KEY=YOUR_OPEN_WEATHER_MAP_API_KEY to local.properties file
     */
    const val API_KEY = BuildConfig.API_KEY
    const val METRIC = "imperial"
    const val WEATHER_ICON_URL = "https://openweathermap.org/img/wn/%s@2x.png"
    const val CITY_NAME_KEY = "CITY_NAME_KEY"
}