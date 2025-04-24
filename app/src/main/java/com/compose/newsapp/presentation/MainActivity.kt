package com.compose.newsapp.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.compose.newsapp.R
import com.compose.newsapp.core.util.Constants
import com.compose.newsapp.core.util.Logger
import com.compose.newsapp.core.util.ThemeUtils
import com.compose.newsapp.data.worker.SyncWorker
import com.compose.newsapp.databinding.ActivityMainBinding
import com.compose.newsapp.presentation.settings.SettingsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint // Mark Activity for Hilt injection
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Use activityViewModels delegate to get ViewModel scoped to Activity
    private val settingsViewModel: SettingsViewModel by viewModels()


    // --- Notification Permission Handling ---
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Logger.d("MainActivity", "Notification permission granted.")
            // Permission is granted. You can now schedule work that shows notifications.
            scheduleBackgroundSync() // Schedule sync after permission granted
        } else {
            Logger.w("MainActivity", "Notification permission denied.")
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision. Show a Snackbar or Dialog.
            Snackbar.make(
                binding.root,
                "Notifications disabled. Sync notifications won't be shown.",
                Snackbar.LENGTH_LONG
            ).show()
            // Still schedule sync, it just won't show notifications
            scheduleBackgroundSync()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level 33+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Logger.d("MainActivity", "Notification permission already granted.")
                // Permission already granted, schedule sync
                scheduleBackgroundSync()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display rationale importance of permission, then request
                // For simplicity, just request directly now
                Logger.d("MainActivity", "Showing rationale (or requesting directly)...")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                Logger.d("MainActivity", "Requesting notification permission...")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Pre-TIRAMISU, permission not needed, schedule sync
            scheduleBackgroundSync()
        }
    }
    // --- End Notification Permission ---


    override fun onCreate(savedInstanceState: Bundle?) {

        // Apply theme *before* super.onCreate and setContentView
        // Observe theme changes and apply them immediately
        lifecycleScope.launch {
            // repeatOnLifecycle ensures collection stops when PAUSED and restarts when RESUMED
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.themeState.collect { themePref ->
                    Logger.d("MainActivity", "Applying theme: $themePref")
                    ThemeUtils.applyTheme(themePref)
                }
            }
        }


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()

        // Ask for permission (which triggers sync scheduling)
        askNotificationPermission()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup the bottom navigation view with navController
        binding.bottomNavigationView.setupWithNavController(navController)

        // Setup ActionBar with navController and configure top-level destinations
        // These destinations will not show the back arrow in the ActionBar
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_headlines, R.id.navigation_search,
                R.id.navigation_bookmarks, R.id.navigation_settings
            )
        )
        // Comment out if you don't want an ActionBar managed by NavController
        // setupActionBarWithNavController(navController, appBarConfiguration)
    }

    // Handle Up navigation (if using ActionBar)
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun scheduleBackgroundSync() {
        lifecycleScope.launch {
            // Get the user-defined interval (or default)
            val syncIntervalHours = settingsViewModel.syncIntervalState.first() // Get current value
            Logger.d("MainActivity", "Scheduling background sync every $syncIntervalHours hours.")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when connected
                // .setRequiresBatteryNotLow(true) // Optional: run only if battery is okay
                .build()

            // Create a periodic work request
            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = syncIntervalHours, // Use interval from settings
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(Constants.WORK_TAG_SYNC_NEWS) // Tag for managing the work
                // Optional: Set backoff policy for retries
                // .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()

            // Enqueue the work as unique periodic work
            // KEEP ensures that if work with the same name exists, it's kept and the new request is ignored.
            // REPLACE would cancel the existing work and schedule the new one. Use KEEP to avoid rescheduling on every app start if interval hasn't changed.
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                Constants.WORK_TAG_SYNC_NEWS, // Unique name for the work
                ExistingPeriodicWorkPolicy.KEEP, // Policy for existing work
                syncWorkRequest
            )
            Logger.d("MainActivity", "Sync work enqueued with policy KEEP.")
        }
    }

}