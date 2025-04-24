package com.compose.newsapp.data.local.db.converter

import androidx.room.TypeConverter
import com.compose.newsapp.data.local.entity.SourceEmbeddable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Converters for Room database
class Converters {

    private val gson = Gson()

    // Convert SourceEmbeddable to JSON String
    @TypeConverter
    fun fromSource(source: SourceEmbeddable?): String? {
        return source?.let { gson.toJson(it) }
    }

    // Convert JSON String back to SourceEmbeddable
    @TypeConverter
    fun toSource(sourceString: String?): SourceEmbeddable? {
        return sourceString?.let {
            try {
//                val type = object : TypeToken<SourceEmbeddable>() {}.type
//                gson.fromJson(it, type)

                gson.fromJson(it, SourceEmbeddable::class.java)

            } catch (e: Exception) {
                // Handle potential parsing errors if needed
                null
            }
        }
    }

// Add converters for Date/Time if storing as Long instead of String
// @TypeConverter
// fun fromTimestamp(value: Long?): Date? { ... }
// @TypeConverter
// fun dateToTimestamp(date: Date?): Long? { ... }

}