package com.example.trmnldisplay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
     */
    data class Loaded(val apiKey: String, val macAddress: String) : SettingsUiState
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
                    combine(repository.apiKey, repository.macAddress) { key, mac ->
                        SettingsUiState.Loaded(key ?: "", mac ?: "")
                    }
                }.collectAsState(initial = SettingsUiState.Loading)

                SettingsScreen(
                    state = uiState,
                    onSave = { key, mac ->
                        scope.launch {
                            repository.saveApiKey(key)
                            repository.saveMacAddress(mac)
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
    onSave: (String, String) -> Unit
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
    onSave: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apiKeyInput by remember(initialApiKey) { mutableStateOf(initialApiKey) }
    var macAddressInput by remember(initialMacAddress) { mutableStateOf(initialMacAddress) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Configuration", style = MaterialTheme.typography.titleLarge)

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

        Button(
            onClick = {
                onSave(apiKeyInput, macAddressInput)
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
