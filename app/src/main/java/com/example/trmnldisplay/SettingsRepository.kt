package com.example.trmnldisplay

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val MAC_ADDRESS = stringPreferencesKey("mac_address")
    }

    val apiKey: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[API_KEY] }

    val macAddress: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[MAC_ADDRESS] }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    suspend fun saveMacAddress(macAddress: String) {
        context.dataStore.edit { preferences ->
            preferences[MAC_ADDRESS] = macAddress
        }
    }
}
