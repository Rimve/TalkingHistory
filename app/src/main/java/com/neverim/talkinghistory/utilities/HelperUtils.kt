package com.neverim.talkinghistory.utilities

import android.content.Context
import android.media.AudioManager
import android.media.AudioTrack
import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.speech.v1.SpeechSettings
import com.neverim.talkinghistory.R
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

    private suspend fun minEdits(vararg nums: Int): Int {
        return Arrays.stream(nums).min().orElse(Int.MAX_VALUE)
    }

    @Throws(IOException::class)
    fun playAudioFromPath(filePath: String?) {
        val recorderSampleRate: Int = Constants.RECORDER_SAMPLE_RATE
        val recorderAudioEncoding = Constants.RECORDER_AUDIO_ENCODING
        val recorderChannelsOut = Constants.RECORDER_CHANNELS_OUT

        if (filePath == null) return

        val file = File(filePath)
        val byteData = ByteArray(file.length().toInt())
        var iS: FileInputStream?

        try {
            iS = FileInputStream(file)
            iS.read(byteData)
            iS.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        // Set and push to audio track
        val intSize = AudioTrack.getMinBufferSize(
            recorderSampleRate,
            recorderChannelsOut,
            recorderAudioEncoding
        )

        val at = AudioTrack(
            AudioManager.STREAM_MUSIC,
            recorderSampleRate,
            recorderChannelsOut,
            recorderAudioEncoding,
            intSize,
            AudioTrack.MODE_STREAM
        )

        if (at != null) {
            at.play()
            // Write the byte array to the track
            at.write(byteData, 0, byteData.size)
            at.stop()
            at.release()
        } else {
            println("Audio track is not initialised ")
        }
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

}