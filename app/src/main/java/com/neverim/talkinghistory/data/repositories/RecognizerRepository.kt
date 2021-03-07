package com.neverim.talkinghistory.data.repositories

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import com.karumi.dexter.Dexter
import com.neverim.talkinghistory.data.models.PermissionsListener
import com.neverim.talkinghistory.utilities.Constants
import com.neverim.talkinghistory.utilities.HelperUtils.authExplicit
import com.neverim.talkinghistory.utilities.HelperUtils.playAudioFromPath
import java.io.*


class RecognizerRepository(private val context: Context) {

    private val sampleRateCandidates = Constants.SAMPLE_RATE_CANDIDATES
    private var recorderSampleRate: Int = Constants.RECORDER_SAMPLE_RATE
    private val recorderChannels = Constants.RECORDER_CHANNELS
    private val recorderAudioEncoding = Constants.RECORDER_AUDIO_ENCODING

    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private var transcriptResult: String? = null
    private var os: FileOutputStream? = null
    private var sData: ByteArray? = null
    private var sizeInBytes = AudioRecord.getMinBufferSize(
        recorderSampleRate,
        recorderChannels,
        recorderAudioEncoding
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
            //playAudioFromPath(audioFilePath)
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

    private fun createAudioRecord(): AudioRecord? {
        for (sampleRate in sampleRateCandidates) {
            sizeInBytes =
                AudioRecord.getMinBufferSize(sampleRate, recorderChannels, recorderAudioEncoding)
            if (sizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
                continue
            }
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate, recorderChannels, recorderAudioEncoding, sizeInBytes
            )
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                sData = ByteArray(sizeInBytes)
                recorderSampleRate = sampleRate
                return audioRecord
            } else {
                audioRecord.release()
            }
        }
        return null
    }

    suspend fun getTranscript(): String? {
        sampleRecognize()
        println("Finished")
        return transcriptResult
    }

    private fun sampleRecognize() {
        SpeechClient.create(authExplicit(context)).use { speechClient ->
            val languageCode = "lt-LT"
            val sampleRateHertz = recorderSampleRate
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
                println(alternative)
                transcriptResult = alternative.transcript.toString()
            }
            println("Transcribing done!")
        }
    }

    fun isRecording() = isRecording

}
