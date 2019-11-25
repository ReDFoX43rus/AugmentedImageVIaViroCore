package com.liberaid.virocoretest

import android.app.Application
import android.os.SystemClock
import timber.log.Timber

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(CustomTimberTree(listOf(LogIntoFileLogInterceptor(applicationContext))))

        Timber.d("App start=${SystemClock.elapsedRealtime()}")
    }

}