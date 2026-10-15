package com.nan.nearbystorefinder.data.local.database

import androidx.room.TypeConverter

/** Stores a List<String> (e.g. place types, opening hours text) as a single delimited column. */
class StringListConverter {
    @TypeConverter
    fun fromList(value: List<String>?): String = value?.joinToString(SEPARATOR) ?: ""

    @TypeConverter
    fun toList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList() else value.split(SEPARATOR)

    companion object {
        private const val SEPARATOR = "\u001F" // unit separator, won't collide with real text
    }
}
