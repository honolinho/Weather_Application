package com.example.weather_application.views

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.example.weather_application.GlideApp
import com.example.weather_application.R
import com.example.weather_application.constants.Constants.WEATHER_ICON_URL
import com.example.weather_application.databinding.ActivityMainBinding
import com.example.weather_application.viewModels.CurrentWeatherInfoState
import com.example.weather_application.viewModels.WeatherViewModel
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupSearchView()
        handleObservers()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        buildLocationRequest()
        buildLocationCallBack()
        requestLocationPermission()
        requestLocationUpdate()
    }

    private fun setupSearchView() {
        val searchView = binding.searchView
        searchView.queryHint = "Search Here"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    showProgressBar()
                    viewModel.getCityCurrentWeather(it)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

    }

    private fun handleObservers() {
        viewModel.weatherInfo.observe(this) {
            updateView(it)
        }
    }

    private fun updateView(data: CurrentWeatherInfoState) {
        hideProgressBar()
        when (data) {
            is CurrentWeatherInfoState.OnDataReady -> {
                val temp = data.weatherInfo.main.temp
                binding.tvTemp.text = getString(R.string.temperature, temp)
                binding.tvCityName.text = data.weatherInfo.name
                val iconUrl = String.format(WEATHER_ICON_URL, data.weatherInfo.weather[0].icon)
                GlideApp.with(this)
                    .load(iconUrl)
                    .into(binding.ivWeather)
            }
            is CurrentWeatherInfoState.OnError -> {
                // Add alert dialog to prompt the user of an error that has occurred

            }
            is CurrentWeatherInfoState.NoInfoFound -> {
                // display an alert to prompt the user we were unable to find the information for the city they entered
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .build()
    }

    private fun requestLocationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            when {
                permissions[ACCESS_COARSE_LOCATION] == true -> {
                    // Only approximate location access granted.
                    requestLocationUpdate()
                }
                else -> {
                    // No location access granted.
                    showProgressBar()
                    viewModel.getSavedCityName()
                }
            }
        }
        locationPermissionRequest.launch(arrayOf(ACCESS_COARSE_LOCATION))
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latitude = location.latitude.toString()
                    val longitude = location.longitude.toString()
                    showProgressBar()
                    viewModel.getCurrentWeatherInfo(long = longitude, lat = latitude)
                }
            }
        }
    }

    private fun requestLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            locationCallback?.let {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    it,
                    Looper.myLooper()
                )
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        //clean up when view is destroyed
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
        super.onDestroy()
    }
}
