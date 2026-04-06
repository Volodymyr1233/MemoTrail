package com.example.memotrail.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "memotrail_prefs")

class UserPreferencesRepository(
    private val context: Context
) {
    val darkModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: false
    }

    val languageTag: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setLanguageTag(languageTag: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = languageTag
        }
    }

    private companion object {
        val KEY_DARK_MODE: Preferences.Key<Boolean> = booleanPreferencesKey("dark_mode_enabled")
        val KEY_LANGUAGE: Preferences.Key<String> = stringPreferencesKey("language_tag")
    }
}

