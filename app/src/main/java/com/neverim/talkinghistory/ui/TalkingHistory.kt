package com.neverim.talkinghistory.ui

import android.Manifest.permission.*
import android.app.Application
import android.content.res.Configuration
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.neverim.talkinghistory.data.models.PermissionsListener


class TalkingHistory : Application() {

    private val LOG_TAG = this.javaClass.simpleName

    override fun onCreate() {
        super.onCreate()
        // Enable offline database
        Log.i(LOG_TAG,"setting database persistence")
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        // Ask for permissions
        Log.i(LOG_TAG,"asking for permissions")
        Dexter.withContext(this)
            .withPermissions(RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
            .withListener(PermissionsListener(this))
            .check()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

}