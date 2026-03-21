package com.nan.nearbystorefinder.domain.repository

import com.nan.nearbystorefinder.domain.model.Store

interface StoreRepository {
    suspend fun getNearbyStores(lat: Double, lng: Double, category: String? = null): List<Store>
    suspend fun searchStores(query: String, lat: Double, lng: Double): List<Store>
}
