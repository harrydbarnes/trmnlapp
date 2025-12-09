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

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Loaded(val apiKey: String, val macAddress: String) : SettingsUiState
}

class SettingsActivity : ComponentActivity() {
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
