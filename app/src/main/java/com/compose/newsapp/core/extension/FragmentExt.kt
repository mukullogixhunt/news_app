package com.compose.newsapp.core.extension

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

/**
 * Hides the software keyboard from the screen for the current Fragment.
 */
fun Fragment.hideKeyboard() {
    // A fragment's view might be null if called at the wrong time,
    // and the activity context is needed to get the system service.
    view?.let { v -> // Ensure the fragment's view is available
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        // Hide the keyboard using the view's window token
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
    }
}

// Optional: You might also want a version that works directly from a View
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0)
}