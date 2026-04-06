package com.example.memotrail.di

import android.content.Context
import com.example.memotrail.data.local.MemoTrailDatabase
import com.example.memotrail.data.preferences.UserPreferencesRepository
import com.example.memotrail.data.repository.TripRepository
import com.example.memotrail.data.repository.TripRepositoryImpl

interface AppContainer {
    val tripRepository: TripRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val database: MemoTrailDatabase = MemoTrailDatabase.create(context)

    override val tripRepository: TripRepository by lazy {
        TripRepositoryImpl(
            tripDao = database.tripDao(),
            tripDayDao = database.tripDayDao(),
            mediaEntryDao = database.mediaEntryDao(),
            tripTagDao = database.tripTagDao()
        )
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }
}

