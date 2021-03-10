package com.neverim.talkinghistory.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.data.repositories.RecognizerRepository

class RecognizerViewModelFactory(private val recognizerRepo: RecognizerRepository, private val context: Context) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecognizerViewModel(recognizerRepo, context) as T
    }

}