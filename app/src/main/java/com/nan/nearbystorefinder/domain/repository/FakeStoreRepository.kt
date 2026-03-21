package com.nan.nearbystorefinder.domain.repository

import com.nan.nearbystorefinder.domain.model.Store

class FakeStoreRepository: StoreRepository {
    override suspend fun getNearbyStores(lat: Double, lng: Double, category: String?): List<Store> {
        val stores = listOf(
            Store(
                id = "1",
                name = "Artisan Crust",
                rating = 4.9f,
                userRatingsTotal = 1200,
                distance = 0.8f,
                category = "FOOD",
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=500&auto=format&fit=crop",
                isOpen = true,
                address = "Downtown Square",
                latitude = lat + 0.005,
                longitude = lng + 0.005
            ),
            Store(
                id = "2",
                name = "Blueberry Roast",
                rating = 4.7f,
                userRatingsTotal = 1200,
                distance = 0.4f,
                category = "COFFEE",
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=500&auto=format&fit=crop",
                isOpen = true,
                address = "High Street",
                latitude = lat - 0.003,
                longitude = lng + 0.002
            ),
            Store(
                id = "3",
                name = "Green Leaf Market",
                rating = 4.5f,
                userRatingsTotal = 850,
                distance = 1.5f,
                category = "GROCERY",
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=500&auto=format&fit=crop",
                isOpen = false,
                address = "West End",
                latitude = lat + 0.01,
                longitude = lng - 0.005
            ),
            Store(
                id = "4",
                name = "The Urban Den",
                rating = 4.9f,
                userRatingsTotal = 420,
                distance = 2.1f,
                category = "FOOD",
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=500&auto=format&fit=crop",
                isOpen = true,
                address = "East Side",
                latitude = lat - 0.008,
                longitude = lng - 0.008
            ),
            Store(
                id = "5",
                name = "Velvet Roast & Co.",
                rating = 4.9f,
                userRatingsTotal = 320,
                distance = 0.4f,
                category = "COFFEE",
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=500&auto=format&fit=crop",
                isOpen = true,
                address = "East Side",
                latitude = lat + 0.002,
                longitude = lng + 0.003
            ),
            Store(
                id = "6",
                name = "Noir Attire",
                rating = 4.7f,
                userRatingsTotal = 560,
                distance = 1.2f,
                category = "GROCERY",
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=500&auto=format&fit=crop",
                isOpen = true,
                address = "Main Street",
                latitude = lat - 0.005,
                longitude = lng + 0.007
            )
        )
        return if (category != null) {
            stores.filter { it.category.equals(category, ignoreCase = true) }
        } else {
            stores
        }
    }

    override suspend fun searchStores(query: String, lat: Double, lng: Double): List<Store> {
        return getNearbyStores(lat, lng).filter {
            it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
        }
    }
}
