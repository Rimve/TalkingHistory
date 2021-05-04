package com.neverim.talkinghistory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.data.StorageRepository

class StorageViewModelFactory(private val storageRepository: StorageRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StorageViewModel(storageRepository) as T
    }

}