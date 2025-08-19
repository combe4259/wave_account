package com.example.accountbook

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WaveAccountApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}