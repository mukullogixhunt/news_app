package com.compose.newsapp.presentation.settings




import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.slider.Slider
import com.compose.newsapp.R
import com.compose.newsapp.core.util.Constants
import com.compose.newsapp.core.util.Logger
import com.compose.newsapp.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    // Use activityViewModels because theme/sync settings affect the whole app (MainActivity)
    private val viewModel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Flag to prevent listener loops during programmatic changes
    private var isUpdatingThemeUi = false
    private var isUpdatingSyncUi = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupThemeSelection()
        setupSyncIntervalSelection()
        observeSettings()
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe Theme
                launch {
                    viewModel.themeState
                        .collectLatest { theme ->
                            updateThemeSelectionUI(theme)
                        }
                }
                // Observe Sync Interval
                launch {
                    viewModel.syncIntervalState
                        .collectLatest { interval ->
                            updateSyncIntervalUI(interval)
                        }
                }
            }
        }
    }

    private fun setupThemeSelection() {
        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            if (!isUpdatingThemeUi) { // Prevent loop if UI update triggers listener
                val newTheme = when (checkedId) {
                    R.id.radio_button_light -> Constants.THEME_LIGHT
                    R.id.radio_button_dark -> Constants.THEME_DARK
                    else -> Constants.THEME_SYSTEM // Default to system
                }
                Logger.d("SettingsFragment","Theme changed by user: $newTheme")
                viewModel.setTheme(newTheme)
            }
        }
    }

    private fun setupSyncIntervalSelection() {
        binding.sliderSyncInterval.addOnChangeListener { slider, value, fromUser ->
            if (fromUser && !isUpdatingSyncUi) {
                val intervalHours = value.roundToLong()
                updateSyncValueText(intervalHours) // Update text immediately for feedback
            }
        }
        // Listener for when the user lifts their finger (commits the value)
        binding.sliderSyncInterval.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Optional: Can add logic here if needed when user starts sliding
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Update ViewModel only when the user stops sliding
                if (!isUpdatingSyncUi) {
                    val intervalHours = slider.value.roundToLong()
                    Logger.d("SettingsFragment","Sync Interval set by user: $intervalHours hours")
                    viewModel.setSyncInterval(intervalHours)
                    // TODO: Trigger rescheduling logic if needed immediately.
                    // E.g., by having MainActivity observe syncIntervalState or using a SharedFlow event.
                    // For now, relying on next app start.
                }
            }
        })
    }

    private fun updateThemeSelectionUI(theme: String) {
        isUpdatingThemeUi = true // Set flag
        Logger.d("SettingsFragment","Updating theme UI to: $theme")
        val checkedId = when (theme) {
            Constants.THEME_LIGHT -> R.id.radio_button_light
            Constants.THEME_DARK -> R.id.radio_button_dark
            else -> R.id.radio_button_system
        }
        binding.radioGroupTheme.check(checkedId)
        isUpdatingThemeUi = false // Clear flag
    }

    private fun updateSyncIntervalUI(intervalHours: Long) {
        isUpdatingSyncUi = true // Set flag
        Logger.d("SettingsFragment","Updating sync UI to: $intervalHours hours")
        binding.sliderSyncInterval.value = intervalHours.toFloat()
        updateSyncValueText(intervalHours)
        isUpdatingSyncUi = false // Clear flag
    }

    private fun updateSyncValueText(intervalHours: Long) {
        binding.textViewSyncValue.text = "Every $intervalHours ${if(intervalHours == 1L) "hour" else "hours"}"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
