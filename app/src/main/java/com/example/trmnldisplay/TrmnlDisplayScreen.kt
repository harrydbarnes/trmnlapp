package com.example.trmnldisplay

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

/**
 * A Composable that displays the TRMNL screen content.
 *
 * This function handles fetching the image URL from the TRMNL API using the provided
 * API Key and MAC Address. It refreshes the image periodically based on the
 * refresh rate returned by the API.
 *
 * @param apiKey The API Key used for authentication with the TRMNL API.
 * @param macAddress The MAC Address used to identify the device.
 * @param modifier The modifier to apply to the container Box.
 */
@Composable
fun TrmnlDisplayScreen(
    apiKey: String?,
    macAddress: String?,
    modifier: Modifier = Modifier
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    // Default refresh to 15 mins if not specified
    var refreshInterval by remember { mutableStateOf(15 * 60 * 1000L) }

    LaunchedEffect(apiKey, macAddress) {
        if (apiKey.isNullOrEmpty() || macAddress.isNullOrEmpty()) {
            error = "Please configure API Key and MAC Address in Settings"
            return@LaunchedEffect
        }

        while (true) {
            try {
                // API Endpoint from docs: https://usetrmnl.com/api/display
                val client = OkHttpClient()
                val url = "https://usetrmnl.com/api/display"

                val requestBuilder = Request.Builder()
                    .url(url)
                    .addHeader("access-token", apiKey)

                // Add MAC address as 'id' header if needed, or just relies on API key?
                // Docs say: "TRMNL /api/display endpoint... requires an API Key to be set as the access-token HTTP header."
                // It doesn't explicitly mention MAC address header for the API call itself if the API key is unique to device.
                // However, the "trmnl-display" emulator code usually sends 'id' (MAC address).
                // We'll add it to be safe as it's standard for their devices.
                requestBuilder.addHeader("id", macAddress)

                // Optional headers mentioned in docs
                requestBuilder.addHeader("battery-voltage", "5.0") // Emulate plugged in?
                requestBuilder.addHeader("wifi-rssi", "-50")
                requestBuilder.addHeader("fw-version", "1.0.0")

                val request = requestBuilder.build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody ?: "{}")

                    if (json.has("image_url")) {
                        imageUrl = json.getString("image_url")

                        // Check for refresh rate
                        // Docs example: "refresh_rate"=>"1800" (seconds?) or minutes?
                        // "reset_duration" was also mentioned.
                        // Let's check both.

                        val refreshRateSeconds = json.optLong("refresh_rate", 0)
                        val resetDurationMinutes = json.optLong("reset_duration", 0)

                        if (refreshRateSeconds > 0) {
                            refreshInterval = refreshRateSeconds * 1000L
                        } else if (resetDurationMinutes > 0) {
                            refreshInterval = resetDurationMinutes * 60 * 1000L
                        } else {
                            // Fallback to 15 minutes
                            refreshInterval = 15 * 60 * 1000L
                        }

                        error = null
                    } else {
                         error = "Invalid response: No image_url found"
                    }
                } else {
                     error = "Error: ${response.code} - ${response.message}"
                }

            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                error = "Failed to fetch: ${e.message}"
                // Retry after 1 minute if failed
                refreshInterval = 60 * 1000L
            }

            delay(refreshInterval)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (error != null) {
            Text(text = error!!, color = Color.White)
        } else if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "TRMNL Screen",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
