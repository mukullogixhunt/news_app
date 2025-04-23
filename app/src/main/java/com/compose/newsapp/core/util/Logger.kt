package com.compose.newsapp.core.util

import android.util.Log
import com.compose.newsapp.BuildConfig

object Logger {
    private const val TAG = "NewsFlowApp"

    fun d(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun w(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) Log.w(tag, message)
    }

    fun e(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
    }
}