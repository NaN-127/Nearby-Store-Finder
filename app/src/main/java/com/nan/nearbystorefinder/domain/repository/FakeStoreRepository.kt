package com.nan.nearbystorefinder.domain.repository

import com.nan.nearbystorefinder.domain.model.Store

class FakeStoreRepository: StoreRepository {
    override suspend fun getNearbyStores(): List<Store> {
        return listOf<Store>(
            Store(
                id = "1",
                name = "Aether Coffee",
                rating = 4.9f,
                distance = 0.4f,
                category = "Cafe",
                imageUrl = "",
                isOpen = true
            ),
            Store(
                id = "2",
                name = "Lumina Atelier",
                rating = 4.8f,
                distance = 0.8f,
                category = "Fashion",
                imageUrl = "",
                isOpen = true
            )

        )
    }
}