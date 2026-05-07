package com.example.memotrail

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.memotrail.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.memotrail.R

@RunWith(AndroidJUnit4::class)
class MemoTrailUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun waitForHomeScreen() {
        val homeLabel = composeTestRule.activity.getString(R.string.home_label)
        composeTestRule.waitUntil(timeoutMillis = 7000) {
            composeTestRule.onAllNodesWithText(homeLabel).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun test1_bottomNavigation_isDisplayed() {
        waitForHomeScreen()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.home_label)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.map_label)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.add_label)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.settings_nav_label)).assertIsDisplayed()
    }

    @Test
    fun test2_navigation_canNavigateToSettings() {
        waitForHomeScreen()
        val settingsNavLabel = composeTestRule.activity.getString(R.string.settings_nav_label)
        val settingsTitle = composeTestRule.activity.getString(R.string.settings_title)
        composeTestRule.onNodeWithText(settingsNavLabel).performClick()
        composeTestRule.onAllNodesWithText(settingsTitle).onFirst().assertIsDisplayed()
    }

    @Test
    fun test3_navigation_canOpenAddTripScreen() {
        waitForHomeScreen()
        val addLabel = composeTestRule.activity.getString(R.string.add_label)
        composeTestRule.onNodeWithText(addLabel).performClick()
        val createTripTitle = composeTestRule.activity.getString(R.string.add_trip_title)
        composeTestRule.onNodeWithText(createTripTitle).assertIsDisplayed()
    }

    @Test
    fun test4_splashScreen_displaysTagline() {
        val tagline = composeTestRule.activity.getString(R.string.splash_tagline)
        composeTestRule.onNodeWithText(tagline).assertExists()
    }

    @Test
    fun test5_navigation_mapTabSelected() {
        waitForHomeScreen()

        val mapLabel = composeTestRule.activity.getString(R.string.map_label)
        composeTestRule.onNodeWithText(mapLabel).performClick()

        composeTestRule.onNodeWithText(mapLabel).assertIsSelected()
    }

    @Test
    fun test6_search_filteringWorks() {
        waitForHomeScreen()
        val searchPlaceholder = composeTestRule.activity.getString(R.string.search_trips_placeholder)

        composeTestRule.onNodeWithText(searchPlaceholder).performTextInput("NonExistentTrip123")

        val moreOptions = composeTestRule.activity.getString(R.string.more_options)
        composeTestRule.onNodeWithContentDescription(moreOptions).assertDoesNotExist()
    }

    @Test
    fun test7_mainScreen_canOpenTripMenu() {
        waitForHomeScreen()

        val moreOptions = composeTestRule.activity.getString(R.string.more_options)
        if (composeTestRule.onAllNodesWithContentDescription(moreOptions).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onAllNodesWithContentDescription(moreOptions).onFirst().performClick()
            val editAction = composeTestRule.activity.getString(R.string.edit_action)
            composeTestRule.onNodeWithText(editAction).assertIsDisplayed()
        }
    }

    @Test
    fun test8_dayForm_validationCheck() {
        waitForHomeScreen()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.add_label)).performClick()
        val saveText = composeTestRule.activity.getString(R.string.save)
        composeTestRule.onNodeWithContentDescription(saveText).performClick()

        val errorText = composeTestRule.activity.getString(R.string.trip_title_required)
        composeTestRule.waitUntil(3000) {
            composeTestRule.onAllNodesWithText(errorText).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(errorText).assertExists()
    }

    @Test
    fun test9_tripDetails_timelineIsVisible() {
        waitForHomeScreen()

        val moreOptions = composeTestRule.activity.getString(R.string.more_options)
        if (composeTestRule.onAllNodesWithContentDescription(moreOptions).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onAllNodesWithContentDescription(moreOptions).onFirst().performClick()

            val timelineTitle = composeTestRule.activity.getString(R.string.journey_timeline_title)
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodesWithText(timelineTitle).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText(timelineTitle).assertIsDisplayed()
        }
    }

    @Test
    fun test10_dayForm_canOpenDatePicker() {
        waitForHomeScreen()
        val moreOptions = composeTestRule.activity.getString(R.string.more_options)
        if (composeTestRule.onAllNodesWithContentDescription(moreOptions).fetchSemanticsNodes()
                .isNotEmpty()
        ) {
            composeTestRule.onAllNodesWithContentDescription(moreOptions).onFirst().performClick()
            val addDayContentDesc = composeTestRule.activity.getString(R.string.add_day)
            composeTestRule.waitUntil(5000) {
                composeTestRule.onAllNodesWithContentDescription(addDayContentDesc)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithContentDescription(addDayContentDesc).performClick()
            val dateLabel = composeTestRule.activity.getString(R.string.day_date_label)
            composeTestRule.onNodeWithText(dateLabel).performClick()

            val confirmText = composeTestRule.activity.getString(R.string.confirm)
            composeTestRule.onNodeWithText(confirmText).assertExists()
        }
    }

    @Test
    fun test11_settings_darkModeOptionVisible() {
        waitForHomeScreen()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.settings_nav_label)).performClick()
        val darkModeLabel = composeTestRule.activity.getString(R.string.dark_mode_label)
        composeTestRule.onNodeWithText(darkModeLabel).assertIsDisplayed()
    }

    @Test
    fun test12_settings_aboutSectionDisplaysVersion() {
        waitForHomeScreen()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.settings_nav_label)).performClick()
        val versionLabel = composeTestRule.activity.getString(R.string.version_label)
        val versionValue = composeTestRule.activity.getString(R.string.app_version_value)
        composeTestRule.onNodeWithText(versionLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(versionValue).assertIsDisplayed()
    }
}