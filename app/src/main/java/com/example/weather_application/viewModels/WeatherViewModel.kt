package com.example.weather_application.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather_application.constants.Constants.CITY_NAME_KEY
import com.example.weather_application.models.dto.CurrentWeatherData
import com.example.weather_application.models.repositories.DataStoreRepository
import com.example.weather_application.models.repositories.WeatherInfoResponse
import com.example.weather_application.models.repositories.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CurrentWeatherInfoState {
    data class OnDataReady(val weatherInfo: CurrentWeatherData) : CurrentWeatherInfoState()
    object OnError : CurrentWeatherInfoState()
    object NoInfoFound : CurrentWeatherInfoState()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val dataStoreRepo: DataStoreRepository
) : ViewModel() {

    private val _weatherInfo: MutableLiveData<CurrentWeatherInfoState> = MutableLiveData()
    val weatherInfo: LiveData<CurrentWeatherInfoState> = _weatherInfo

    fun getCityCurrentWeather(cityName: String) {
        viewModelScope.launch {
            when (val response = repository.getCityCurrentWeather(cityName)) {
                is WeatherInfoResponse.OnSuccess -> {
                    response.currentData?.let {
                        /**
                         * Given more time rewrite this so we can save only if a new city is entered
                         * Also since we are re using this function when searching for previously saved cityName
                         * identify a flag to make sure we are saving only when the user searches for a new input
                         **/
                        dataStoreRepo.putString(CITY_NAME_KEY, cityName)
                        _weatherInfo.value = CurrentWeatherInfoState.OnDataReady(it)
                    } ?: kotlin.run { _weatherInfo.value = CurrentWeatherInfoState.OnError }
                }
                is WeatherInfoResponse.NetworkError -> {
                    _weatherInfo.value = CurrentWeatherInfoState.OnError
                }
                is WeatherInfoResponse.NoGeoCoordinateFound -> {
                    _weatherInfo.value = CurrentWeatherInfoState.NoInfoFound
                }
            }
        }
    }

    fun getCurrentWeatherInfo(long: String, lat: String) {
        viewModelScope.launch {
            when (val response = repository.getCurrentWeatherInfo(long = long, lat = lat)) {
                is WeatherInfoResponse.OnSuccess -> {
                    response.currentData?.let {
                        _weatherInfo.value = CurrentWeatherInfoState.OnDataReady(it)
                    } ?: kotlin.run { _weatherInfo.value = CurrentWeatherInfoState.NoInfoFound }
                }
                is WeatherInfoResponse.NetworkError -> {
                    _weatherInfo.value = CurrentWeatherInfoState.OnError
                }
                is WeatherInfoResponse.NoGeoCoordinateFound -> {
                    _weatherInfo.value = CurrentWeatherInfoState.NoInfoFound
                }
            }
        }
    }

    fun getSavedCityName() {
        viewModelScope.launch {
            val cityName = dataStoreRepo.getString(CITY_NAME_KEY)
            if (cityName.isNotEmpty()) {
                getCityCurrentWeather(cityName)
            } else _weatherInfo.value = CurrentWeatherInfoState.NoInfoFound
        }
    }
}
