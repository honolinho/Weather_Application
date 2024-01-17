package com.example.weather_application

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weather_application.models.dto.CurrentWeatherData
import com.example.weather_application.models.dto.Main
import com.example.weather_application.models.dto.Weather
import com.example.weather_application.models.repositories.WeatherInfoResponse
import com.example.weather_application.models.repositories.WeatherRepository
import com.example.weather_application.viewModels.CurrentWeatherInfoState
import com.example.weather_application.viewModels.WeatherViewModel
import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain


import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class WeatherViewModelTest {
    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var repository: WeatherRepository

    @MockK
    private lateinit var ioDispatcher: CoroutineDispatcher

    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = WeatherViewModel(repository)
    }

    @Test
    fun `GIVEN the user enters a city name AND clicks on search THEN the weather information is returned`() {
        val cityName = "Chicago"
        coEvery { repository.getCityCurrentWeather(cityName) } returns WeatherInfoResponse.OnSuccess(
            getWeatherData()
        )
        viewModel.getCityCurrentWeather(cityName)
        val actual = viewModel.weatherInfo.value
        Truth.assertThat(actual).isInstanceOf(CurrentWeatherInfoState::class.java)
        (actual as CurrentWeatherInfoState.OnDataReady)
        Truth.assertThat(actual.weatherInfo.weather).isEqualTo(getWeatherData().weather)
        Truth.assertThat(actual.weatherInfo.name).isEqualTo(getWeatherData().name)
        Truth.assertThat(actual.weatherInfo.main.temp).isEqualTo(getWeatherData().main.temp)
    }

    @Test
    fun `GIVEN the user enters an unknown city AND clicks search THEN no weather information is returned`() {
        val cityName = "jkjkbj"
        coEvery { repository.getCityCurrentWeather(cityName) } returns WeatherInfoResponse.NoGeoCoordinateFound
        viewModel.getCityCurrentWeather(cityName)
        val actual = viewModel.weatherInfo.value
        Truth.assertThat(actual).isInstanceOf(CurrentWeatherInfoState.NoInfoFound::class.java)
    }

    @Test
    fun `GIVEN the user has allowed location permissions THEN fetch user's current location weather information`() {
        coEvery {
            repository.getCurrentWeatherInfo(
                allAny(),
                allAny()
            )
        } returns WeatherInfoResponse.OnSuccess(getWeatherData())
        viewModel.getCurrentWeatherInfo("-122.0907", "37.4414")
        val actual = viewModel.weatherInfo.value
        Truth.assertThat(actual).isInstanceOf(CurrentWeatherInfoState::class.java)
        (actual as CurrentWeatherInfoState.OnDataReady)
        Truth.assertThat(actual.weatherInfo.weather).isEqualTo(getWeatherData().weather)
        Truth.assertThat(actual.weatherInfo.name).isEqualTo(getWeatherData().name)
        Truth.assertThat(actual.weatherInfo.main.temp).isEqualTo(getWeatherData().main.temp)
    }


    private fun getWeatherData(): CurrentWeatherData {
        return CurrentWeatherData(
            name = "Chicago",
            Main(2.4),
            listOf(Weather(description = "clouds", id = 803, icon = "04d"))
        )
    }
}
