package com.compose.newsapp.core.util

import com.compose.newsapp.BuildConfig

object Constants {

    const val NEWS_API_BASE_URL = "https://newsapi.org/"
    // IMPORTANT: Add NEWS_API_KEY="YOUR_ACTUAL_API_KEY" to your local.properties file
    // Make sure local.properties is in .gitignore
    const val NEWS_API_KEY = BuildConfig.NEWS_API_KEY

    const val DATABASE_NAME = "newsflow_db"
    const val DATABASE_VERSION = 1

    const val PREFS_NAME = "newsflow_prefs"
    const val PREF_KEY_THEME = "pref_theme" // e.g., "light", "dark", "system"
    const val PREF_KEY_SYNC_INTERVAL = "pref_sync_interval" // e.g., value in hours

    const val DEFAULT_SYNC_INTERVAL_HOURS = 6L
    const val WORK_TAG_SYNC_NEWS = "sync_news_work"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    const val PAGE_SIZE = 20 // NewsAPI default/max page size for free tier often 20 or 100 total

}