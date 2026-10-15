package com.nan.nearbystorefinder.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nan.nearbystorefinder.data.local.dao.FavoriteDao
import com.nan.nearbystorefinder.data.local.entity.StoreEntity

// Version bumped 1 -> 2: Store/StoreEntity gained several new fields (types,
// photoAttributions, openingHoursText, priceLevel, phoneNumber, websiteUri,
// googleMapsUri) and rating/userRatingsTotal/isOpen became nullable as part of
// the Google Places API (New) migration. No migration path is defined since
// favorites is a local-only cache with no server-side source of truth;
// destructive fallback simply clears previously saved favorites on upgrade.
@Database(entities = [StoreEntity::class], version = 2, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val favoriteDao: FavoriteDao
}
