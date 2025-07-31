package com.habitiora.batty

import android.app.Application
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant
import javax.inject.Inject

@HiltAndroidApp
class BattyApp: Application() {
    //@Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
            Log.i("MoniFlexApp", "onCreate: Timber inicializado")
        } else {
            Log.i("MoniFlexApp", "onCreate: Timber inicializado en modo producción")
            plant(CrashReportingTree())
        }
//        WorkManager.initialize(
//            this,
//            workManagerConfiguration
//        )
    }

//    override val workManagerConfiguration: Configuration
//        get() = Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()

    /** A tree which logs important information for crash reporting.  */
    private class CrashReportingTree : Timber.Tree() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) return
                val messageFirebase = "$tag: $message, ${t?.message}"
                crashlytics.log(messageFirebase)
                if (t != null) {
                    crashlytics.recordException(t)
                }
        }
    }
}