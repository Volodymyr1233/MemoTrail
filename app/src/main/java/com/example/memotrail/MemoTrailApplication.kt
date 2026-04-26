package com.example.memotrail

import android.app.Application
import android.content.pm.PackageManager
import com.example.memotrail.di.AppContainer
import com.example.memotrail.di.DefaultAppContainer
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.android.libraries.places.api.Places

class MemoTrailApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        setupCoil()
        val apiKey = resolveMapsApiKey()
        if (!Places.isInitialized() && apiKey.isNotBlank() && !apiKey.contains("REPLACE", ignoreCase = true)) {
            Places.initialize(this, apiKey)
        }
        container = DefaultAppContainer(this)
    }

    private fun setupCoil() {
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .crossfade(true)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.2)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizeBytes(256L * 1024L * 1024L)
                        .build()
                }
                .build()
        )
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

