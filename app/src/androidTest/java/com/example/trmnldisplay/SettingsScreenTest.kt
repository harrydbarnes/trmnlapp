package com.example.trmnldisplay

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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

        // Assert that the loading indicator is displayed using its test tag.
        composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
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
