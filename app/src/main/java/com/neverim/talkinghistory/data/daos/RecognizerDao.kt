package com.neverim.talkinghistory.data.daos

import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.api.gax.rpc.ClientStream
import com.google.api.gax.rpc.ResponseObserver
import com.google.api.gax.rpc.StreamController
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import com.neverim.talkinghistory.utilities.Constants
import com.neverim.talkinghistory.utilities.HelperUtils

class RecognizerDao {

    private val LOG_TAG = this.javaClass.simpleName

    private val sampleRateCandidates = Constants.SAMPLE_RATE_CANDIDATES
    private var recorderSampleRate: Int = Constants.RECORDER_SAMPLE_RATE
    private val recorderChannels = Constants.RECORDER_CHANNELS
    private val recorderAudioEncoding = Constants.RECORDER_AUDIO_ENCODING

    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecognizing = false
    private var sData: ByteArray? = null
    private var sizeInBytes = 6400
//        AudioRecord.getMinBufferSize(
//        recorderSampleRate,
//        recorderChannels,
//        recorderAudioEncoding
//    )

    private val transcription: String? = null
    private val mutableTranscription = MutableLiveData<String>()

    init {
        mutableTranscription.value = transcription
    }

    fun isRecognizing() = isRecognizing
    fun getTranscript() = mutableTranscription as LiveData<String>

    fun audioSetup(context: Context): Boolean {
        Log.i(LOG_TAG, "checking if microphone is available")
        return if (HelperUtils.hasMicrophone(context)) {
            true
        } else {
            Log.i(LOG_TAG, "microphone is not available")
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
            Log.i(LOG_TAG, "stopping recognition")
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
                        Log.i(LOG_TAG, "response: ${alternative.transcript}")
                    }

                    override fun onComplete() {
                        for (response in responses) {
                            val result = response.resultsList[0]
                            val alternative = result.alternativesList[0]
                            Log.i(LOG_TAG, "completed transcript session: ${alternative.transcript}")
                        }
                    }

                    override fun onError(t: Throwable) {
                        Log.e(LOG_TAG, t.toString())
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
                recorder = createAudioRecord()
                recorder!!.startRecording()
                Log.i(LOG_TAG, "starting recognition")
                val startTime = System.currentTimeMillis()

                // Audio Input Stream
                while (isRecognizing) {
                    val estimatedTime = System.currentTimeMillis() - startTime
                    val data = ByteArray(sizeInBytes)
                    recorder!!.read(data, 0, sizeInBytes)
                    if (estimatedTime > 60000) { // Audio stream can not exceed 60 seconds
                        Log.i(LOG_TAG, "stopping recognition")
                        stopRecognition()
                        break
                    }
                    request = StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(data))
                        .build()
                    clientStream.send(request)
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(LOG_TAG, e.toString())
        }
        responseObserver!!.onComplete()
    }

}