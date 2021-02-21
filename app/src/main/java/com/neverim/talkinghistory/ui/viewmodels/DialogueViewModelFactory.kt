package com.neverim.talkinghistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.data.repositories.AdjacenciesRepository

class DialogueViewModelFactory(private val adjacenciesRepository: AdjacenciesRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DialogueViewModel(adjacenciesRepository) as T
    }
}