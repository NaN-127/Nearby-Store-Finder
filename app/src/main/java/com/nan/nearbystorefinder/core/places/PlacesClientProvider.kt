package com.nan.nearbystorefinder.core.places

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

/**
 * Initializes the Places SDK for Android (New) exactly once and exposes the
 * singleton [PlacesClient] used by [com.nan.nearbystorefinder.data.remote.repository.GooglePlacesStoreRepository].
 *
 * The API key here is restricted in Google Cloud Console to this app's
 * package name + SHA-1 certificate fingerprint, which is only enforceable
 * through the native Places SDK (not raw REST calls) - see the key-security
 * writeup in the migration report for details.
 */
class PlacesClientProvider(
    private val context: Context,
    private val apiKey: String
) {
    val client: PlacesClient by lazy {
        if (!Places.isInitialized()) {
            if (apiKey.isBlank()) {
                Log.e("PlacesClientProvider", "PLACES_API_KEY is missing. Add it to local.properties.")
            }
            Places.initializeWithNewPlacesApiEnabled(context, apiKey)
        }
        Places.createClient(context)
    }
}
