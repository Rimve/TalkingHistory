package com.neverim.talkinghistory.ui

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import com.google.firebase.database.FirebaseDatabase


class TalkingHistory : Application() {

    private val LOG_TAG = this.javaClass.simpleName

    override fun onCreate() {
        super.onCreate()
        // Enable offline database
        Log.i(LOG_TAG, "setting database persistence")
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    companion object {
        @Volatile private var instance: TalkingHistory? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: TalkingHistory().also { instance = it }
            }
    }

}