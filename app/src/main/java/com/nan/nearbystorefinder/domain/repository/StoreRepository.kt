package com.nan.nearbystorefinder.domain.repository

import com.nan.nearbystorefinder.domain.model.Store

interface StoreRepository {
    suspend fun getNearbyStores(): List<Store>
}