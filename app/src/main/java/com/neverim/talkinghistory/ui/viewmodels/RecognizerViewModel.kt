package com.neverim.talkinghistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.neverim.talkinghistory.data.repositories.RecognizerRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class RecognizerViewModel(private val recognizerRepo: RecognizerRepository) : ViewModel() {
    private fun getTranscript() = recognizerRepo.getTranscript()
    fun audioSetup() = recognizerRepo.audioSetup()
    fun recordAudio() = recognizerRepo.recordAudio()
    fun stopAudio() = recognizerRepo.stopAudio()
    fun isRecording() = recognizerRepo.isRecording()
    fun sampleRecognize(): String? {
        runBlocking {
            val job = GlobalScope.async {
                recognizerRepo.sampleRecognize()
            }
            job.await()
        }
        return getTranscript()
    }
}