package com.neverim.talkinghistory.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.neverim.talkinghistory.data.repositories.RecognizerRepository

class RecognizerViewModel(private val recognizerRepo: RecognizerRepository) : ViewModel() {

    fun audioSetup(context: Context) = recognizerRepo.audioSetup(context)
    fun startRecognition(context: Context) = recognizerRepo.startRecognition(context)
    fun stopRecognition() = recognizerRepo.stopRecognition()
    fun isRecognizing() = recognizerRepo.isRecognizing()
    fun getTranscript() = recognizerRepo.getTranscript()

}