package com.example.palmscanner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class — required by Hilt.
 * Hilt generates a component here that lives for the full app lifecycle.
 * Referenced in AndroidManifest.xml via android:name=".PalmScannerApp"
 */
@HiltAndroidApp
class PalmScannerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // No manual init needed — Hilt handles DI graph creation automatically
    }
}