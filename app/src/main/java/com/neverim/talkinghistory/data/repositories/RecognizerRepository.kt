package com.neverim.talkinghistory.data.repositories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import com.karumi.dexter.Dexter
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.models.PermissionsListener
import kotlinx.coroutines.*
import java.io.*


class RecognizerRepository(private val context: Context) {

    private val SAMPLE_RATE_CANDIDATES = intArrayOf(16000, 11025, 22050, 44100)
    private var RECORDER_SAMPLERATE: Int = 16000
    private val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
    private val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private val RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO

    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var recognizingThread: Thread? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private var transcriptResult: String? = null
    private var os: FileOutputStream? = null
    private var sData: ByteArray? = null
    private var sizeInBytes = AudioRecord.getMinBufferSize(
        RECORDER_SAMPLERATE,
        RECORDER_CHANNELS,
        RECORDER_AUDIO_ENCODING
    )

    private fun hasMicrophone(): Boolean {
        val pManager = context.packageManager
        return pManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    fun audioSetup(): Boolean {
        return if (hasMicrophone()) {
            audioFilePath = context.getExternalFilesDir(null)?.absolutePath + "/audio.pcm"
            Dexter.withContext(context)
                .withPermissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(PermissionsListener(context))
                .check()
            true
        } else {
            false
        }
    }

    fun recordAudio() {
        recorder = createAudioRecord()
        recorder!!.startRecording()
        isRecording = true
        recordingThread = Thread({ writeAudioDataToFile() }, "AudioRecorder Thread")
        recordingThread!!.start()
    }

    fun stopAudio() {
        if (null != recorder) {
            isRecording = false
            recorder!!.stop()
            recorder!!.release()
            recorder = null
            recordingThread = null
            //playShortAudioFileViaAudioTrack(audioFilePath)
        }
    }

    private fun writeAudioDataToFile() {
        sData = ByteArray(sizeInBytes)
        try {
            os = FileOutputStream(audioFilePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        while (isRecording) {
            recorder!!.read(sData!!, 0, sizeInBytes)
            println("Writing to file $audioFilePath")
            try {
                os!!.write(sData, 0, sizeInBytes)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            os!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun playShortAudioFileViaAudioTrack(filePath: String?) {
        if (filePath == null) return
        val file = File(filePath)
        val byteData = ByteArray(file.length().toInt())
        println("File size: ${file.length()}")
        var iS: FileInputStream? = null

        try {
            iS = FileInputStream(file)
            iS.read(byteData)
            iS.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        // Set and push to audio track
        val intSize = AudioTrack.getMinBufferSize(
            RECORDER_SAMPLERATE,
            RECORDER_CHANNELS_OUT,
            RECORDER_AUDIO_ENCODING
        )

        val at = AudioTrack(
            AudioManager.STREAM_MUSIC,
            RECORDER_SAMPLERATE,
            RECORDER_CHANNELS_OUT,
            RECORDER_AUDIO_ENCODING,
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

    private fun createAudioRecord(): AudioRecord? {
        for (sampleRate in SAMPLE_RATE_CANDIDATES) {
            sizeInBytes =
                AudioRecord.getMinBufferSize(sampleRate, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING)
            if (sizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
                continue
            }
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, sizeInBytes
            )
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                sData = ByteArray(sizeInBytes)
                RECORDER_SAMPLERATE = sampleRate
                return audioRecord
            } else {
                audioRecord.release()
            }
        }
        return null
    }

    @Throws(IOException::class)
    fun authExplicit(): SpeechSettings {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.auth)
        val credentialsProvider: CredentialsProvider = FixedCredentialsProvider
            .create(ServiceAccountCredentials.fromStream(inputStream))
        return SpeechSettings.newBuilder()
            .setCredentialsProvider(credentialsProvider)
            .build()
    }

    suspend fun getTranscript(): String? {
        sampleRecognize()
        println("Finished")
        return transcriptResult
    }

    private fun sampleRecognize() {
        SpeechClient.create(authExplicit()).use { speechClient ->
            val languageCode = "lt-LT"
            val sampleRateHertz = RECORDER_SAMPLERATE
            val encoding: RecognitionConfig.AudioEncoding =
                RecognitionConfig.AudioEncoding.LINEAR16
            val config: RecognitionConfig = RecognitionConfig.newBuilder()
                .setLanguageCode(languageCode)
                .setSampleRateHertz(sampleRateHertz)
                .setEncoding(encoding)
                .build()
            val file = File(audioFilePath!!)
            val byteData = ByteArray(file.length().toInt())
            var iS: FileInputStream? = null
            try {
                iS = FileInputStream(file)
                iS.read(byteData)
                iS.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            val content = ByteString.copyFrom(byteData)
            val audio: RecognitionAudio = RecognitionAudio.newBuilder()
                .setContent(content)
                .build()
            val request: RecognizeRequest = RecognizeRequest.newBuilder()
                .setConfig(config)
                .setAudio(audio)
                .build()
            val response: RecognizeResponse = speechClient.recognize(request)
            for (result in response.resultsList) {
                // First alternative is the most probable result
                val alternative: SpeechRecognitionAlternative =
                    result.alternativesList[0]
                println("Transcript: $alternative\n")
                transcriptResult = alternative.toString()
            }
            println("Transcribing done!")
        }
    }


    fun isRecording() = isRecording
}
