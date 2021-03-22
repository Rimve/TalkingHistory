package com.neverim.talkinghistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.data.repositories.CharacterRepository
import com.neverim.talkinghistory.data.repositories.StorageRepository

class DialogueViewModelFactory(private val adjacenciesRepository: CharacterRepository,
                               private val storageRepository: StorageRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DialogueViewModel(adjacenciesRepository, storageRepository) as T
    }
}