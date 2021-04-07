package com.neverim.talkinghistory.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.*
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.neverim.talkinghistory.R


class TalkingHistory : Application() {

    private val LOG_TAG = this.javaClass.simpleName

    private lateinit var connectivityManager: ConnectivityManager
    private var currentActivity: Activity? = null
    private var alert: AlertDialog? = null

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = activity
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}

        })
        // Enable offline database
        Log.i(LOG_TAG, "setting database persistence")
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        // Registering network connection callback
        connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (alert != null) {
                    alert!!.dismiss()
                    alert = null
                }
            }

            override fun onLost(network: Network?) {
                if (alert == null) {
                    showAlert()
                }
            }

            override fun onUnavailable() {
                if (alert == null) {
                    showAlert()
                }
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    private fun showAlert() {
        val dialogBuilder = AlertDialog.Builder(currentActivity)
        dialogBuilder.setMessage("Make sure that Wi-Fi or mobile data is turned on, then try again")
            .setCancelable(false)
        alert = dialogBuilder.create()
        alert!!.setTitle("No Internet Connection")
        alert!!.setIcon(R.drawable.ic_bad_connection)
        alert!!.show()
    }

}