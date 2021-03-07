package com.neverim.talkinghistory.ui

import android.Manifest.permission.*
import android.app.Application
import android.content.res.Configuration
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.neverim.talkinghistory.data.models.PermissionsListener


class TalkingHistory : Application() {

    override fun onCreate() {
        super.onCreate()
        // Enable offline database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        // Ask for permissions
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