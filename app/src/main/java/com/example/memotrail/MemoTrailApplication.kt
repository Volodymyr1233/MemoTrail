package com.example.memotrail

import android.app.Application
import android.content.pm.PackageManager
import com.example.memotrail.di.AppContainer
import com.example.memotrail.di.DefaultAppContainer
import com.google.android.libraries.places.api.Places

class MemoTrailApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val apiKey = resolveMapsApiKey()
        if (!Places.isInitialized() && apiKey.isNotBlank() && !apiKey.contains("REPLACE", ignoreCase = true)) {
            Places.initialize(this, apiKey)
        }
        container = DefaultAppContainer(this)
    }

    private fun resolveMapsApiKey(): String {
        val manifestKey = try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
        } catch (_: Exception) {
            ""
        }

        if (manifestKey.isNotBlank() && !manifestKey.contains("REPLACE", ignoreCase = true)) {
            return manifestKey
        }

        // Fallback for local/dev setups that still keep key in resources.
        return getString(R.string.google_maps_api_key)
    }
}

