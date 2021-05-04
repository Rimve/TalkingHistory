package com.neverim.talkinghistory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.data.CharacterRepository

class CharacterViewModelFactory(private val adjacenciesRepository: CharacterRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CharacterViewModel(adjacenciesRepository) as T
    }

}