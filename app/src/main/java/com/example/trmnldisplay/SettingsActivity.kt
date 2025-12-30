package com.example.trmnldisplay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the settings screen.
 */
sealed interface SettingsUiState {
    /**
     * State representing that the settings are currently being loaded.
     */
    data object Loading : SettingsUiState

    /**
     * State representing that the settings have been loaded.
     *
     * @property apiKey The saved API Key for the TRMNL service.
     * @property macAddress The saved MAC Address of the TRMNL device.
     * @property isCustomMode Whether custom mode is enabled.
     * @property customImageUrl The custom image URL.
     * @property customRefreshRate The custom refresh rate in seconds.
     */
    data class Loaded(
        val apiKey: String,
        val macAddress: String,
        val isCustomMode: Boolean,
        val customImageUrl: String,
        val customRefreshRate: Long
    ) : SettingsUiState
}

/**
 * Activity for configuring the application settings.
 *
 * Allows the user to view and edit the TRMNL API Key and MAC Address, and provides
 * a link to the external TRMNL configuration website.
 */
class SettingsActivity : ComponentActivity() {
    /**
     * Called when the activity is starting.
     *
     * Initializes the activity and sets up the Compose UI for the settings screen.
     * It observes the API Key and MAC Address from [SettingsRepository] and passes
     * the current state to the [SettingsScreen].
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then
     * this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val repository = remember { SettingsRepository(context) }
                val scope = rememberCoroutineScope()

                val uiState by remember(repository) {
                    combine(
                        repository.apiKey,
                        repository.macAddress,
                        repository.isCustomMode,
                        repository.customImageUrl,
                        repository.customRefreshRate
                    ) { key, mac, isCustom, url, rate ->
                        SettingsUiState.Loaded(
                            key ?: "",
                            mac ?: "",
                            isCustom,
                            url ?: "",
                            rate
                        )
                    }
                }.collectAsState(initial = SettingsUiState.Loading)

                SettingsScreen(
                    state = uiState,
                    onSave = { key, mac, isCustom, url, rate ->
                        scope.launch {
                            repository.saveApiKey(key)
                            repository.saveMacAddress(mac)
                            repository.saveCustomSettings(isCustom, url, rate)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Composable function that renders the settings screen.
 *
 * Displays a loading indicator or the settings content based on the provided [state].
 *
 * @param state The current state of the settings UI (Loading or Loaded).
 * @param onSave A callback function to be invoked when the user saves the settings.
 *               It takes the new API Key and MAC Address as arguments.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onSave: (String, String, Boolean, String, Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("TRMNL Settings") })
        }
    ) { padding ->
        when (state) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag("loadingIndicator"))
                }
            }
            is SettingsUiState.Loaded -> {
                SettingsContent(
                    initialApiKey = state.apiKey,
                    initialMacAddress = state.macAddress,
                    initialIsCustomMode = state.isCustomMode,
                    initialCustomImageUrl = state.customImageUrl,
                    initialCustomRefreshRate = state.customRefreshRate,
                    onSave = onSave,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

/**
 * Composable function that renders the content of the settings screen when loaded.
 *
 * Provides input fields for the API Key and MAC Address, and a button to save these settings.
 * Also includes a button to open the external TRMNL configuration website.
 *
 * @param initialApiKey The initial value for the API Key input field.
 * @param initialMacAddress The initial value for the MAC Address input field.
 * @param onSave A callback function to be invoked when the user clicks the Save button.
 *               It takes the updated API Key and MAC Address as arguments.
 * @param modifier The modifier to apply to this layout.
 */
@Composable
fun SettingsContent(
    initialApiKey: String,
    initialMacAddress: String,
    initialIsCustomMode: Boolean,
    initialCustomImageUrl: String,
    initialCustomRefreshRate: Long,
    onSave: (String, String, Boolean, String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apiKeyInput by remember(initialApiKey) { mutableStateOf(initialApiKey) }
    var macAddressInput by remember(initialMacAddress) { mutableStateOf(initialMacAddress) }
    var isCustomMode by remember(initialIsCustomMode) { mutableStateOf(initialIsCustomMode) }
    var customImageUrlInput by remember(initialCustomImageUrl) { mutableStateOf(initialCustomImageUrl) }
    var customRefreshRateInput by remember(initialCustomRefreshRate) { mutableStateOf(initialCustomRefreshRate.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Connection Mode", style = MaterialTheme.typography.titleLarge)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isCustomMode) "Custom URL" else "Official Cloud",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = isCustomMode,
                onCheckedChange = { isCustomMode = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Configuration", style = MaterialTheme.typography.titleLarge)

        if (isCustomMode) {
            OutlinedTextField(
                value = customImageUrlInput,
                onValueChange = { customImageUrlInput = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("e.g., http://192.168.1.50/image.png") }
            )

            OutlinedTextField(
                value = customRefreshRateInput,
                onValueChange = { if (it.all { char -> char.isDigit() }) customRefreshRateInput = it },
                label = { Text("Refresh Rate (seconds)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Interval to refresh the image") }
            )
        } else {
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Found in your TRMNL account settings") }
            )

            OutlinedTextField(
                value = macAddressInput,
                onValueChange = { macAddressInput = it },
                label = { Text("MAC Address") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("MAC Address of the device to emulate") }
            )
        }

        Button(
            onClick = {
                val refreshRate = customRefreshRateInput.toLongOrNull() ?: 900L
                onSave(apiKeyInput, macAddressInput, isCustomMode, customImageUrlInput, refreshRate)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("External Links", style = MaterialTheme.typography.titleLarge)

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://usetrmnl.com/"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Configure Plugins (usetrmnl.com)")
        }
    }
}
