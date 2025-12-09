package com.example.trmnldisplay

import android.app.Application

/**
 * The main Application class for the TRMNL Display app.
 *
 * This class serves as the base class for the application and is responsible for
 * maintaining global application state.
 */
class TrmnlApp : Application() {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     *
     * Use this method for global initialization.
     */
    override fun onCreate() {
        super.onCreate()
    }
}
