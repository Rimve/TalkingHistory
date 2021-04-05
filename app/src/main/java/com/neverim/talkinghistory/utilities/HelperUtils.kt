package com.neverim.talkinghistory.utilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.speech.v1.SpeechSettings
import com.karumi.dexter.Dexter
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.models.PermissionsListener
import java.io.*
import java.util.*


object HelperUtils {

    suspend fun levDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) {
            IntArray(
                str2.length + 1
            )
        }
        for (i in 0..str1.length) {
            for (j in 0..str2.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    dp[i][j] = minEdits(
                        dp[i - 1][j - 1] + numOfReplacement(str1[i - 1], str2[j - 1]), // replace
                        dp[i - 1][j] + 1, // delete
                        dp[i][j - 1] + 1 // insert
                    )
                }
            }
        }
        return dp[str1.length][str2.length]
    }

    private suspend fun numOfReplacement(c1: Char, c2: Char): Int {
        return if (c1 == c2) 0 else 1
    }

    private suspend fun minEdits(vararg ints: Int): Int {
        return Arrays.stream(ints).min().orElse(Int.MAX_VALUE)
    }

    @Throws(IOException::class)
    fun authExplicit(context: Context): SpeechSettings {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.auth)
        val credentialsProvider: CredentialsProvider = FixedCredentialsProvider
            .create(ServiceAccountCredentials.fromStream(inputStream))
        return SpeechSettings.newBuilder()
            .setCredentialsProvider(credentialsProvider)
            .build()
    }

    fun hasMicrophone(context: Context): Boolean {
        val pManager = context.packageManager
        return pManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    fun checkPermissions(context: Context) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(PermissionsListener(context))
            .check()
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap =
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

}