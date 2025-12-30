package com.example.trmnldisplay

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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

        /**
         * Key for storing the custom mode flag.
         */
        val IS_CUSTOM_MODE = booleanPreferencesKey("is_custom_mode")

        /**
         * Key for storing the custom image URL.
         */
        val CUSTOM_IMAGE_URL = stringPreferencesKey("custom_image_url")

        /**
         * Key for storing the custom refresh rate in seconds.
         */
        val CUSTOM_REFRESH_RATE = longPreferencesKey("custom_refresh_rate")
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
     * A Flow emitting whether custom mode is enabled.
     */
    val isCustomMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[IS_CUSTOM_MODE] ?: false }

    /**
     * A Flow emitting the custom image URL, or null if not set.
     */
    val customImageUrl: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[CUSTOM_IMAGE_URL] }

    /**
     * A Flow emitting the custom refresh rate in seconds. Defaults to 900 (15 minutes).
     */
    val customRefreshRate: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[CUSTOM_REFRESH_RATE] ?: 900L }

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

    /**
     * Saves the custom settings to the DataStore.
     *
     * @param isCustom Whether custom mode is enabled.
     * @param url The custom image URL.
     * @param refreshRate The custom refresh rate in seconds.
     */
    suspend fun saveCustomSettings(isCustom: Boolean, url: String, refreshRate: Long) {
        context.dataStore.edit { preferences ->
            preferences[IS_CUSTOM_MODE] = isCustom
            preferences[CUSTOM_IMAGE_URL] = url
            preferences[CUSTOM_REFRESH_RATE] = refreshRate
        }
    }
}
