package com.example.palmscanner

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class PalmScannerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // No manual init needed — Hilt handles DI graph creation automatically
    }
}