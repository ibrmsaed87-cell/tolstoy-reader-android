package com.spinel.tolstoyreader

import android.app.Application
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH_LOG", "Uncaught exception in thread ${thread.name}", throwable)
            throwable.printStackTrace()
        }
    }
}
