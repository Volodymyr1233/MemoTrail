package com.example.memotrail

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.memotrail.ui.app.MemoTrailApp
import com.example.memotrail.ui.theme.MemoTrailTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val appContainer = (application as MemoTrailApplication).container

        // Read persisted prefs once so recreation (e.g., locale switch) does not briefly flash defaults.
        val (persistedDarkModeEnabled, persistedLanguageTag) = runBlocking {
            val prefs = appContainer.userPreferencesRepository
            prefs.darkModeEnabled.first() to prefs.languageTag.first()
        }

        // Apply locale before super.onCreate so Activity resources start in the correct language.
        val currentPrimaryLanguage = AppCompatDelegate
            .getApplicationLocales()
            .get(0)
            ?.language
            .orEmpty()
        if (persistedLanguageTag.isNotBlank() && currentPrimaryLanguage != persistedLanguageTag) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(persistedLanguageTag))
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkModeEnabled by appContainer.userPreferencesRepository
                .darkModeEnabled
                .collectAsStateWithLifecycle(initialValue = persistedDarkModeEnabled)

            MemoTrailTheme(darkTheme = darkModeEnabled) {
                MemoTrailApp(appContainer = appContainer)
            }
        }
    }
}
