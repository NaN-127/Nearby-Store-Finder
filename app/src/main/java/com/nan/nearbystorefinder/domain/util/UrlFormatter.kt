package com.nan.nearbystorefinder.domain.util

import java.net.URI

object UrlFormatter {

    /**
     * Formats a potentially long, complex website URL into a clean, human-readable
     * base URL (e.g. "https://www.walgreens.com/") for concise UI display,
     * while the original URL is preserved for browser redirection.
     */
    fun formatDisplayUrl(rawUrl: String?): String {
        if (rawUrl.isNullOrBlank()) return ""
        val trimmed = rawUrl.trim()

        return try {
            val withScheme = if (!trimmed.startsWith("http://", ignoreCase = true) &&
                !trimmed.startsWith("https://", ignoreCase = true)
            ) {
                "https://$trimmed"
            } else {
                trimmed
            }

            val uri = URI(withScheme)
            val host = uri.host
            if (!host.isNullOrBlank()) {
                val scheme = uri.scheme?.lowercase() ?: "https"
                val port = if (uri.port != -1 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""
                "$scheme://$host$port/"
            } else {
                val clean = withScheme.removePrefix("http://").removePrefix("https://")
                val domain = clean.substringBefore('/')
                "https://$domain/"
            }
        } catch (e: Exception) {
            val clean = trimmed.removePrefix("http://").removePrefix("https://")
            val domain = clean.substringBefore('/')
            if (domain.isNotBlank()) "https://$domain/" else trimmed
        }
    }
}
