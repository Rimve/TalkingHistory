package com.neverim.talkinghistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverim.talkinghistory.data.repositories.RecognizerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecognizerViewModel(private val recognizerRepo: RecognizerRepository) : ViewModel() {
    fun audioSetup() = recognizerRepo.audioSetup()
    fun recordAudio() = recognizerRepo.recordAudio()
    fun stopAudio() = recognizerRepo.stopAudio()
    fun isRecording() = recognizerRepo.isRecording()
    fun sampleRecognize() {
        viewModelScope.launch(Dispatchers.IO) {
            recognizerRepo.sampleRecognize()
        }
    }
}