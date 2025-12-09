package com.example.trmnldisplay

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * The main entry point of the application.
 *
 * This activity configures the window for immersive mode (hiding system bars), keeps the screen on,
 * and sets up the main UI content which includes the TRMNL display screen and a settings button.
 */
class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is starting.
     *
     * This method initializes the activity, sets up the window flags for full-screen and screen-on behavior,
     * and sets the Jetpack Compose content. It retrieves user settings (API key and MAC address) via
     * [SettingsRepository] and passes them to the [TrmnlDisplayScreen].
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then
     * this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable full screen / immersive mode
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        // Keep screen on if it's AOD mode (configurable?)
        // For now, we'll just keep it on as requested for AOD use case
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val repository = remember { SettingsRepository(context) }
                val apiKey by repository.apiKey.collectAsState(initial = null)
                val macAddress by repository.macAddress.collectAsState(initial = null)

                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            startActivity(Intent(this, SettingsActivity::class.java))
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                        TrmnlDisplayScreen(
                            apiKey = apiKey,
                            macAddress = macAddress,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
