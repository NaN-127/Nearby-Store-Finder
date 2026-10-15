package com.nan.nearbystorefinder.core.maps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

/**
 * Launches Google Maps directions for a place. Kept entirely separate from
 * any card/detail composable so navigation-launch logic isn't tangled with
 * visual components.
 *
 * Uses Google's documented "Universal cross-platform maps URL" format
 * (https://developers.google.com/maps/documentation/urls/get-started#directions-action).
 * Because the URL is a plain https link, Android resolves it to the Google
 * Maps app when installed, and falls back to any browser automatically when
 * it isn't - satisfying the "browser fallback" requirement without manual
 * package-existence checks.
 */
object MapsNavigator {

    fun openDirections(
        context: Context,
        destinationLat: Double,
        destinationLng: Double,
        destinationPlaceId: String? = null,
        originLat: Double? = null,
        originLng: Double? = null
    ) {
        val builder = StringBuilder("https://www.google.com/maps/dir/?api=1")
        builder.append("&destination=").append(destinationLat).append(",").append(destinationLng)
        if (!destinationPlaceId.isNullOrBlank()) {
            builder.append("&destination_place_id=").append(Uri.encode(destinationPlaceId))
        }
        if (originLat != null && originLng != null) {
            builder.append("&origin=").append(originLat).append(",").append(originLng)
        }
        builder.append("&travelmode=driving")

        val intent = Intent(Intent.ACTION_VIEW, builder.toString().toUri())
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No app or browser can handle it (extremely unlikely on a real device) - fail silently
            // rather than crash; callers may optionally show a toast/snackbar on failure in the future.
        }
    }
}
