package com.example.memotrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.memotrail.ui.app.MemoTrailApp
import com.example.memotrail.ui.theme.MemoTrailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as MemoTrailApplication).container

        setContent {
            val darkModeEnabled by appContainer.userPreferencesRepository
                .darkModeEnabled
                .collectAsStateWithLifecycle(initialValue = false)
            val languageTag by appContainer.userPreferencesRepository
                .languageTag
                .collectAsStateWithLifecycle(initialValue = "en")

            LaunchedEffect(languageTag) {
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(languageTag)
                )
            }

            MemoTrailTheme(darkTheme = darkModeEnabled) {
                MemoTrailApp(appContainer = appContainer)
            }
        }
    }
}
