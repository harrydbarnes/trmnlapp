package com.example.trmnldisplay

import android.service.dreams.DreamService
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * A DreamService implementation that displays the TRMNL screen as a screensaver (Daydream).
 *
 * This service implements the necessary Lifecycle and SavedStateRegistry interfaces
 * to support hosting Jetpack Compose content directly within a DreamService.
 */
class TrmnlDreamService : DreamService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    /**
     * Returns the [ViewModelStore] of the provider.
     */
    override val viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    /**
     * Returns the [Lifecycle] of the provider.
     */
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    /**
     * Returns the [SavedStateRegistry] of the provider.
     */
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    /**
     * Called when the service is created.
     *
     * Initializes the SavedStateRegistry and the Lifecycle.
     */
    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    /**
     * Called when the dream is attached to the window.
     *
     * Sets up the dream window properties (interactive, fullscreen) and initializes the Compose content.
     * It creates a [ComposeView], binds the lifecycle owners, and sets the [TrmnlDisplayScreen] as the content.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isInteractive = false
        isFullscreen = true

        val settingsRepository = SettingsRepository(this)

        val contentView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(this@TrmnlDreamService)
            setViewTreeViewModelStoreOwner(this@TrmnlDreamService)
            setViewTreeSavedStateRegistryOwner(this@TrmnlDreamService)

            setContent {
                // Observe flows directly in Compose, avoiding runBlocking
                val apiKey by settingsRepository.apiKey.collectAsState(initial = null)
                val macAddress by settingsRepository.macAddress.collectAsState(initial = null)

                TrmnlDisplayScreen(
                    apiKey = apiKey,
                    macAddress = macAddress
                )
            }
        }
        setContentView(contentView)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    /**
     * Called when the dream is detached from the window.
     *
     * Handles the teardown of the Lifecycle and ViewModelStore.
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }
}

/**
 * Helper interface to combine lifecycle interfaces needed for Compose.
 *
 * This effectively just shadows [androidx.lifecycle.LifecycleOwner] but is kept here
 * to match the original structure and ensure explicit typing if needed.
 */
interface LifecycleOwner : androidx.lifecycle.LifecycleOwner
