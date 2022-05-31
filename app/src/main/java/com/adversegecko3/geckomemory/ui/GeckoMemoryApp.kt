package com.adversegecko3.geckomemory.ui

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.adversegecko3.geckomemory.data.Preferences

class GeckoMemoryApp: Application() {
    companion object {
        lateinit var mAppContext: Context
        lateinit var mResources: Resources
        lateinit var mPrefs: Preferences
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize the companion object variables with the applicationContext, the system resources and the Preferences

        mAppContext = applicationContext
        mResources = resources
        mPrefs = Preferences(applicationContext)
    }
}