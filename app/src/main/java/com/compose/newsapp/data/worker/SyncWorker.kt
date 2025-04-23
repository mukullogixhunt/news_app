package com.compose.newsapp.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.compose.newsapp.core.util.Logger
import com.compose.newsapp.domain.usecase.article.SyncTopHeadlinesUseCase
import com.compose.newsapp.presentation.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    // Inject UseCases or Repositories needed
    private val syncTopHeadlinesUseCase: SyncTopHeadlinesUseCase,
    // Inject the notification helper
    private val notificationHelper: NotificationHelper
    // Inject ResourceProvider if needing strings for notification
    // private val resourceProvider: ResourceProvider
): CoroutineWorker(appContext, workerParams){
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) { // Ensure work runs on IO dispatcher
        try {
            Logger.d("SyncWorker", "Background sync starting...")

            // TODO: Get user preferences (country/category) if needed for sync
            // val country = settingsRepository.getCountry().first() ...

            // Execute the sync use case (which calls the repository)
            val newArticlesCount = syncTopHeadlinesUseCase.execute() // Uses defaults for now

            if (newArticlesCount > 0) {
                Logger.d("SyncWorker", "Sync found $newArticlesCount new articles. Showing notification.")
                // TODO: Use ResourceProvider for translatable strings
                val title = "NewsFlow Updates"
                val content = "Found $newArticlesCount new headlines!"
                notificationHelper.showSyncNotification(title, content)
            } else {
                Logger.d("SyncWorker", "Sync completed. No new articles found.")
            }

            Result.success()

        } catch (e: Exception) {
            Logger.e("SyncWorker", "Sync failed", e)
            // Decide whether to retry or fail based on the exception
            if (runAttemptCount < 3) { // Simple retry limit
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }


}