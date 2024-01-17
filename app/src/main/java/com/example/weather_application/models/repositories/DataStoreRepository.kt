package com.example.weather_application.models.repositories


interface DataStoreRepository {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String
}