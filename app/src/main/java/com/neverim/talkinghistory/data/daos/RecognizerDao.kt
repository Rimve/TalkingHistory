package com.neverim.talkinghistory.data.daos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.api.gax.rpc.ClientStream
import com.google.api.gax.rpc.ResponseObserver
import com.google.api.gax.rpc.StreamController
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import com.karumi.dexter.Dexter
import com.neverim.talkinghistory.data.models.PermissionsListener
import com.neverim.talkinghistory.utilities.Constants
import com.neverim.talkinghistory.utilities.HelperUtils
import kotlin.system.exitProcess

class RecognizerDao {

    private val sampleRateCandidates = Constants.SAMPLE_RATE_CANDIDATES
    private var recorderSampleRate: Int = Constants.RECORDER_SAMPLE_RATE
    private val recorderChannels = Constants.RECORDER_CHANNELS
    private val recorderAudioEncoding = Constants.RECORDER_AUDIO_ENCODING

    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var audioFilePath: String? = null
    private var isRecognizing = false
    private var sData: ByteArray? = null
    private var sizeInBytes = AudioRecord.getMinBufferSize(
        recorderSampleRate,
        recorderChannels,
        recorderAudioEncoding
    )

    private val transcription: String? = null
    private val mutableTranscription = MutableLiveData<String>()

    init {
        mutableTranscription.value = transcription
    }

    fun isRecognizing() = isRecognizing
    fun getTranscript() = mutableTranscription as LiveData<String>

    private fun hasMicrophone(context: Context): Boolean {
        val pManager = context.packageManager
        return pManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    fun audioSetup(context: Context): Boolean {
        return if (hasMicrophone(context)) {
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

    fun startRecognition(context: Context) {
        isRecognizing = true
        recordingThread = Thread({ streamingMicRecognize(context) }, "Recognizer Thread")
        recordingThread!!.start()
    }

    fun stopRecognition() {
        if (null != recorder) {
            isRecognizing = false
            recorder!!.stop()
            recorder!!.release()
            recorder = null
            recordingThread = null
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

    // LIVE RECOGNITION

    @Throws(java.lang.Exception::class)
    private fun streamingMicRecognize(context: Context) {
        var responseObserver: ResponseObserver<StreamingRecognizeResponse>? = null
        try {
            SpeechClient.create(HelperUtils.authExplicit(context)).use { client ->
                responseObserver = object : ResponseObserver<StreamingRecognizeResponse> {
                    var responses: ArrayList<StreamingRecognizeResponse> = ArrayList()
                    override fun onStart(controller: StreamController) {}
                    override fun onResponse(response: StreamingRecognizeResponse) {
                        responses.add(response)
                        val result = response.resultsList[0]
                        val alternative = result.alternativesList[0]
                        mutableTranscription.postValue(alternative.transcript)
                        println(alternative.transcript)
                    }

                    override fun onComplete() {
                        for (response in responses) {
                            val result = response.resultsList[0]
                            val alternative = result.alternativesList[0]
                            println(alternative.transcript)
                        }
                    }

                    override fun onError(t: Throwable) {
                        println(t)
                    }
                }
                val clientStream: ClientStream<StreamingRecognizeRequest> =
                    client.streamingRecognizeCallable().splitCall(responseObserver)
                val languageCode = Constants.LANGUAGE_CODEC
                val sampleRateHertz = recorderSampleRate
                val encoding: RecognitionConfig.AudioEncoding =
                    RecognitionConfig.AudioEncoding.LINEAR16
                val recognitionConfig: RecognitionConfig = RecognitionConfig.newBuilder()
                    .setLanguageCode(languageCode)
                    .setSampleRateHertz(sampleRateHertz)
                    .setEncoding(encoding)
                    .build()
                val streamingRecognitionConfig =
                    StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build()
                var request = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingRecognitionConfig)
                    .build()
                clientStream.send(request)
                if (!hasMicrophone(context)) {
                    println("Microphone not supported")
                    exitProcess(0)
                }
                recorder = createAudioRecord()
                recorder!!.startRecording()
                println("Start speaking")
                val startTime = System.currentTimeMillis()
                // Audio Input Stream
                recorder!!.read(sData!!, 0, sizeInBytes)
                while (isRecognizing) {
                    val estimatedTime = System.currentTimeMillis() - startTime
                    val data = ByteArray(6400)
                    recorder!!.read(data, 0, 6400)
                    if (estimatedTime > 60000) { // 60 seconds
                        println("Stop speaking.")
                        recorder!!.stop()
                        recorder!!.release()
                        recorder = null
                        break
                    }
                    request = StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(data))
                        .build()
                    clientStream.send(request)
                }
            }
        } catch (e: java.lang.Exception) {
            println(e)
        }
        responseObserver!!.onComplete()
    }

}