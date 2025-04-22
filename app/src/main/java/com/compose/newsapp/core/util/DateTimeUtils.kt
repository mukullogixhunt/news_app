//package com.compose.newsapp.core.util
//
//import android.os.Build
//import java.time.OffsetDateTime
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import java.time.format.FormatStyle
//import java.util.Locale
//
//object DateTimeUtils {
//
//    // Example formatter: "Apr 15, 2024, 10:30 AM" based on device locale
//    private val friendlyDateTimeFormatter: DateTimeFormatter by lazy {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
//                .withLocale(Locale.getDefault())
//                .withZone(ZoneId.systemDefault())
//        } else {
//            // Fallback or use ThreeTenABP library for older APIs
//            DateTimeFormatter.ISO_OFFSET_DATE_TIME // Basic fallback
//        }
//    }
//
//    // Example formatter: "Just now", "5 min ago", "1 hour ago", "Yesterday", "Apr 15"
//    // This requires more complex logic, often using libraries like kotlinx-datetime or custom code.
//    // Keeping it simple for now.
//
//    /**
//     * Parses an ISO 8601 string (like "2024-04-15T10:30:00Z") and formats it
//     * into a user-friendly date/time string based on locale.
//     */
//    fun formatIsoDateTime(isoDateTimeString: String?): String {
//        if (isoDateTimeString == null) return "N/A"
//        return try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val offsetDateTime = OffsetDateTime.parse(isoDateTimeString)
//                offsetDateTime.format(friendlyDateTimeFormatter)
//            } else {
//                // Basic fallback without proper formatting
//                isoDateTimeString.substringBefore("T") // Just get the date part
//            }
//        } catch (e: Exception) {
//            Logger.e("DateTimeUtils", "Failed to parse or format date: $isoDateTimeString", e)
//            "Invalid Date"
//        }
//    }
//
//    // Add more formatting functions as needed (e.g., relative time)
//}