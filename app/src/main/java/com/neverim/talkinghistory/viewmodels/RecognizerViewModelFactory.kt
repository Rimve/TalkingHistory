package com.neverim.talkinghistory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.data.RecognizerRepository

class RecognizerViewModelFactory(private val recognizerRepo: RecognizerRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecognizerViewModel(recognizerRepo) as T
    }

}