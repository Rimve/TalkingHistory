package com.neverim.talkinghistory.ui

import android.app.Application
import android.content.res.Configuration
import com.google.firebase.database.FirebaseDatabase

class TalkingHistory : Application() {

    override fun onCreate() {
        super.onCreate()
        // Enable offline database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    override fun onConfigurationChanged ( newConfig : Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }
}