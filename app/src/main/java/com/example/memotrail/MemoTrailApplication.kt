package com.example.memotrail

import android.app.Application
import com.example.memotrail.di.AppContainer
import com.example.memotrail.di.DefaultAppContainer

class MemoTrailApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}

