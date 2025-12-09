package com.example.trmnldisplay

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Extension property to access the DataStore instance for the application context.
 * The DataStore is named "settings".
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for managing application settings, specifically the TRMNL API Key and MAC Address.
 *
 * This class handles reading from and writing to the Jetpack DataStore.
 *
 * @param context The application context used to access the DataStore.
 */
class SettingsRepository(private val context: Context) {
    /**
     * Companion object containing the DataStore keys.
     */
    companion object {
        /**
         * Key for storing the API Key.
         */
        val API_KEY = stringPreferencesKey("api_key")
        /**
         * Key for storing the MAC Address.
         */
        val MAC_ADDRESS = stringPreferencesKey("mac_address")
    }

    /**
     * A Flow emitting the saved API Key, or null if not set.
     */
    val apiKey: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[API_KEY] }

    /**
     * A Flow emitting the saved MAC Address, or null if not set.
     */
    val macAddress: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[MAC_ADDRESS] }

    /**
     * Saves the provided API Key to the DataStore.
     *
     * @param apiKey The API Key to save.
     */
    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    /**
     * Saves the provided MAC Address to the DataStore.
     *
     * @param macAddress The MAC Address to save.
     */
    suspend fun saveMacAddress(macAddress: String) {
        context.dataStore.edit { preferences ->
            preferences[MAC_ADDRESS] = macAddress
        }
    }
}
