package com.nan.nearbystorefinder.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log.e
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.nan.nearbystorefinder.domain.model.UserLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class LocationClient(
    private val context: Context
) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): UserLocation?{
        return try {
            var location = client.lastLocation.await()

            if(location == null){
                location = client.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token
                ).await()
            }
            location?.let {
                UserLocation(location.latitude,location.longitude)
            }

        }catch (e: Exception){
             null
        }
    }

    suspend fun getAddressFromCoords(lat: Double, long: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(lat, long, 1) { addresses ->
                            continuation.resume(addresses)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, long, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    return@withContext "${address.locality ?: ""}, ${address.adminArea ?: ""}".trim(',', ' ')
                }

                "Location Unavailable"
            } catch (e: Exception) {
                "Location Unavailable"
            }
        }
    }


}
