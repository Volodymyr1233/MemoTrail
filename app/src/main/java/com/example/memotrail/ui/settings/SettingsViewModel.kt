package com.example.memotrail.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memotrail.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val darkModeEnabled: Boolean = false,
    val languageTag: String = "en"
)

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.darkModeEnabled,
                userPreferencesRepository.languageTag
            ) { darkModeEnabled, languageTag ->
                SettingsUiState(darkModeEnabled = darkModeEnabled, languageTag = languageTag)
            }.collect { _uiState.value = it }
        }
    }

    fun onDarkModeChanged(enabled: Boolean) {
        _uiState.update { it.copy(darkModeEnabled = enabled) }
        viewModelScope.launch { userPreferencesRepository.setDarkModeEnabled(enabled) }
    }

    fun onLanguageChanged(languageTag: String) {
        _uiState.update { it.copy(languageTag = languageTag) }
        viewModelScope.launch { userPreferencesRepository.setLanguageTag(languageTag) }
    }

    class Factory(
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

