package com.compose.newsapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NewsFlowApplication : Application(), Configuration.Provider {

    @Inject // Ask Hilt to inject the WorkerFactory
    lateinit var workerFactory: HiltWorkerFactory

    // Provide the Hilt-injected WorkerFactory to WorkManager
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO) // Optional: Adjust log level
            .build()

    override fun onCreate() {
        super.onCreate()
        // TODO: Initialize Theme based on preferences (will do later)
        // TODO: Initialize Timber or other logging libraries if used
    }

}