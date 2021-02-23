package com.neverim.talkinghistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.data.repositories.RecognizerRepository

class RecognizerViewModelFactory(private val recognizerRepo: RecognizerRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecognizerViewModel(recognizerRepo) as T
    }

}