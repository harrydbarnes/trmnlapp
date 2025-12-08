package com.example.trmnldisplay

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_loadingState_showsProgressBar() {
        composeTestRule.setContent {
            SettingsScreen(
                state = SettingsUiState.Loading,
                onSave = { _, _ -> }
            )
        }

        // CircularProgressIndicator doesn't have default text, but we can check if it exists or check that text fields are absent.
        // Checking absence of text fields
        composeTestRule.onNodeWithText("API Key").assertDoesNotExist()
        composeTestRule.onNodeWithText("MAC Address").assertDoesNotExist()
    }

    @Test
    fun settingsScreen_loadedState_showsFieldsWithValues() {
        val apiKey = "test_api_key"
        val macAddress = "test_mac_address"

        composeTestRule.setContent {
            SettingsScreen(
                state = SettingsUiState.Loaded(apiKey, macAddress),
                onSave = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("API Key").assertIsDisplayed()
        composeTestRule.onNodeWithText(apiKey).assertIsDisplayed()
        composeTestRule.onNodeWithText("MAC Address").assertIsDisplayed()
        composeTestRule.onNodeWithText(macAddress).assertIsDisplayed()
    }
}
