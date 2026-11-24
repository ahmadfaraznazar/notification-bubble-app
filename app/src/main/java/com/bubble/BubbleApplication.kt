package com.bubble

import android.app.Application

/**
 * Core Application entry point.
 * Initializes logging, dependency singletons, and notification channels.
 */
class BubbleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialization hooks for core services and logging
    }
}
