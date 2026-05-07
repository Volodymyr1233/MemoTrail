package com.example.memotrail.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import com.example.memotrail.data.preferences.UserPreferencesRepository
import com.example.memotrail.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

	@get:Rule
	val mainDispatcherRule = MainDispatcherRule()

	private lateinit var repository: UserPreferencesRepository
	private lateinit var darkModeFlow: MutableStateFlow<Boolean>
	private lateinit var languageFlow: MutableStateFlow<String>

	@Before
	fun setUp() {
		repository = mockk(relaxed = true)
		darkModeFlow = MutableStateFlow(false)
		languageFlow = MutableStateFlow("en")
		every { repository.darkModeEnabled } returns darkModeFlow
		every { repository.languageTag } returns languageFlow
	}

	@After
	fun tearDown() {
		runCatching { unmockkStatic(AppCompatDelegate::class) }
	}

	@Test
	fun `uiState reflects repository flows`() = runTest(mainDispatcherRule.dispatcher) {
		val viewModel = SettingsViewModel(repository)

		advanceUntilIdle()
		assertEquals(false, viewModel.uiState.value.darkModeEnabled)
		assertEquals("en", viewModel.uiState.value.languageTag)

		darkModeFlow.value = true
		languageFlow.value = "pl"

		advanceUntilIdle()
		assertEquals(true, viewModel.uiState.value.darkModeEnabled)
		assertEquals("pl", viewModel.uiState.value.languageTag)
	}

	@Test
	fun `onDarkModeChanged updates state and persists`() = runTest(mainDispatcherRule.dispatcher) {
		val viewModel = SettingsViewModel(repository)

		viewModel.onDarkModeChanged(true)
		darkModeFlow.value = true
		advanceUntilIdle()

		assertTrue(viewModel.uiState.value.darkModeEnabled)
		coVerify { repository.setDarkModeEnabled(true) }
	}

	@Test
	fun `onLanguageChanged updates state persists and sets locales`() = runTest(mainDispatcherRule.dispatcher) {
		mockkStatic(AppCompatDelegate::class)
		every { AppCompatDelegate.setApplicationLocales(any()) } returns Unit

		val viewModel = SettingsViewModel(repository)
		viewModel.onLanguageChanged("pl")
		languageFlow.value = "pl"
		advanceUntilIdle()

		assertEquals("pl", viewModel.uiState.value.languageTag)
		coVerify { repository.setLanguageTag("pl") }
		verify {
			AppCompatDelegate.setApplicationLocales(match { it.toLanguageTags() == "pl" })
		}
	}
}
