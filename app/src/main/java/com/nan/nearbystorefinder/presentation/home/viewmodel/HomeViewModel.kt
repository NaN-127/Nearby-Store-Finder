package com.nan.nearbystorefinder.presentation.home.viewmodel

import android.system.Os.stat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.util.CoilUtils.result
import com.google.android.play.integrity.internal.n
import com.nan.nearbystorefinder.core.location.LocationClient
import com.nan.nearbystorefinder.domain.model.UserLocation
import com.nan.nearbystorefinder.domain.repository.FakeStoreRepository
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import com.nan.nearbystorefinder.presentation.home.state.HomeState
import kotlinx.coroutines.launch

class HomeViewModel(
    private val locationClient: LocationClient
): ViewModel() {

    var userLocation by mutableStateOf<UserLocation?>(null)
        private set

    var state by mutableStateOf(HomeState())
        private set

    init {
        fetchUserLocation()
    }



    fun fetchUserLocation(){
        viewModelScope.launch {
            val coords = locationClient.getCurrentLocation()
            if(coords!= null){
                val addressName = locationClient.getAddressFromCoords(coords.latitude,coords.longitude)
                userLocation = coords.copy(address = addressName)
            }else{
                userLocation = UserLocation(0.0, 0.0, "Location Unavailable")
            }
        }
    }


}