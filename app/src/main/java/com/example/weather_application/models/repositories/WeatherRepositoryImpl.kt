package com.example.weather_application.models.repositories

import com.example.weather_application.models.dto.*
import com.example.weather_application.models.services.ApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

sealed class WeatherInfoResponse {
    data class OnSuccess(val currentData: CurrentWeatherData?) : WeatherInfoResponse()
    object NetworkError : WeatherInfoResponse()
    object NoGeoCoordinateFound : WeatherInfoResponse()
}

class WeatherRepositoryImpl @Inject constructor(
    private val service: ApiService,
    private val ioDispatcher: CoroutineDispatcher
) : WeatherRepository {

    override suspend fun getCityCurrentWeather(cityName: String): WeatherInfoResponse =
        withContext(ioDispatcher) {
            try {
                val geoResponse = service.getGeocoding(cityName)
                // val geoResponse = geoCodingResponse.await()
                if (geoResponse.isSuccessful) {
                    requestWeatherInfo(geoResponse.body())
                } else WeatherInfoResponse.NetworkError
            } catch (e: Exception) {
                e.printStackTrace()
                WeatherInfoResponse.NetworkError
            }
        }

    override suspend fun getCurrentWeatherInfo(long: String, lat: String): WeatherInfoResponse =
        withContext(ioDispatcher) {
            val response = service.getCurrentWeatherInfo(lat, long)
            if (response.isSuccessful) {
                val extractedResp = mapWeatherInfo(response.body())
                WeatherInfoResponse.OnSuccess(extractedResp)
            } else WeatherInfoResponse.NetworkError
        }

    private suspend fun requestWeatherInfo(resp: List<GeocodingDTO>?): WeatherInfoResponse {
        return if (resp != null && resp.isNotEmpty()) {
            val lat = resp[0].lat.toString()
            val long = resp[0].lon.toString()
            val response = service.getCurrentWeatherInfo(lat, long)
            if (response.isSuccessful) {
                val extractedResp = mapWeatherInfo(response.body())
                WeatherInfoResponse.OnSuccess(extractedResp)
            } else WeatherInfoResponse.NetworkError
        } else WeatherInfoResponse.NoGeoCoordinateFound
    }

    private fun mapWeatherInfo(dataInfo: CurrentWeatherDataDTO?): CurrentWeatherData {
        return CurrentWeatherData(
            name = dataInfo?.name ?: "",
            main = mapMainData(dataInfo?.main),
            weather = mapWeatherData(dataInfo?.weather)
        )
    }

    private fun mapMainData(data: MainDTO?): Main {
        return Main(temp = data?.temp ?: 0.0)
    }

    private fun mapWeatherData(data: List<WeatherDTO>?): List<Weather> {
        val weatherList = mutableListOf<Weather>()
        data?.forEach {
            weatherList.add(
                Weather(
                    description = it.description ?: "",
                    id = it.id ?: 0,
                    icon = it.icon ?: ""
                )
            )
        }
        return weatherList
    }
}
