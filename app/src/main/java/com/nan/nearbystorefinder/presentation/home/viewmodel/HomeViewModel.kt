package com.nan.nearbystorefinder.presentation.home.viewmodel

import android.system.Os.stat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nan.nearbystorefinder.domain.repository.FakeStoreRepository
import com.nan.nearbystorefinder.domain.repository.StoreRepository
import com.nan.nearbystorefinder.presentation.home.state.HomeState
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: StoreRepository
): ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    init {
        loadData()
    }



    fun loadData(){
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val repo = repository.getNearbyStores()
            state = state.copy(stores = repo, isLoading = false)


        }
    }
}