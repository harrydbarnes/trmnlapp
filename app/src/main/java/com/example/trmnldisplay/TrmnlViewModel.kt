package com.example.trmnldisplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface TrmnlUiState {
    data object Loading : TrmnlUiState
    data class Success(val imageUrl: String) : TrmnlUiState
    data class Error(val message: String) : TrmnlUiState
}

class TrmnlViewModel(
    private val repository: SettingsRepository,
    private val api: TrmnlApi = trmnlApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrmnlUiState>(TrmnlUiState.Loading)
    val uiState: StateFlow<TrmnlUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Use collectLatest to cancel the previous polling loop if settings change
            combine(repository.apiKey, repository.macAddress) { key, mac ->
                Pair(key, mac)
            }.collectLatest { (apiKey, macAddress) ->
                if (apiKey.isNullOrEmpty() || macAddress.isNullOrEmpty()) {
                    _uiState.value = TrmnlUiState.Error("Please configure API Key and MAC Address in Settings")
                } else {
                    pollDisplay(apiKey, macAddress)
                }
            }
        }
    }

    private suspend fun pollDisplay(apiKey: String, macAddress: String) {
        _uiState.value = TrmnlUiState.Loading
        var refreshInterval = 15 * 60 * 1000L // Default 15 min

        while (true) {
            try {
                val response = api.getDisplay(apiKey, macAddress)

                if (response.imageUrl != null) {
                     _uiState.value = TrmnlUiState.Success(response.imageUrl)

                     // Update refresh interval
                     val seconds = response.refreshRateSeconds ?: 0L
                     val minutes = response.resetDurationMinutes ?: 0L

                     if (seconds > 0) {
                         refreshInterval = seconds * 1000L
                     } else if (minutes > 0) {
                         refreshInterval = minutes * 60 * 1000L
                     }
                } else {
                    _uiState.value = TrmnlUiState.Error("Invalid response: No image_url found")
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                // Log?
                _uiState.value = TrmnlUiState.Error("Failed to fetch: ${e.message}")
                refreshInterval = 60 * 1000L // Retry in 1 min
            }

            delay(refreshInterval)
        }
    }
}

class TrmnlViewModelFactory(
    private val repository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrmnlViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrmnlViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
