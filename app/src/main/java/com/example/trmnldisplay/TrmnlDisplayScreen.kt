package com.example.trmnldisplay

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * A Composable that displays the TRMNL screen content.
 *
 * This function handles displaying the UI state provided by the [TrmnlViewModel].
 *
 * @param viewModel The ViewModel that holds the UI state and manages business logic.
 * @param modifier The modifier to apply to the container Box.
 */
@Composable
fun TrmnlDisplayScreen(
    viewModel: TrmnlViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is TrmnlUiState.Loading -> {
                CircularProgressIndicator(color = Color.White)
            }
            is TrmnlUiState.Success -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(state.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "TRMNL Screen",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is TrmnlUiState.Error -> {
                Text(text = state.message, color = Color.White)
            }
        }
    }
}
