package com.astro.autotapper

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AutoTapperApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}

