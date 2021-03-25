package com.neverim.talkinghistory.utilities

import com.neverim.talkinghistory.data.Database
import com.neverim.talkinghistory.data.repositories.CharacterRepository
import com.neverim.talkinghistory.data.repositories.RecognizerRepository
import com.neverim.talkinghistory.data.repositories.StorageRepository
import com.neverim.talkinghistory.ui.viewmodels.CharacterViewModelFactory
import com.neverim.talkinghistory.ui.viewmodels.RecognizerViewModelFactory
import com.neverim.talkinghistory.ui.viewmodels.StorageViewModelFactory

object InjectorUtils {

    fun provideCharacterViewModelFactory(): CharacterViewModelFactory {
        val adjacenciesRepository = CharacterRepository.getInstance(Database.getInstance().characterDao)
        return CharacterViewModelFactory(adjacenciesRepository)
    }

    fun provideRecognizerViewModelFactory(): RecognizerViewModelFactory {
        val recognizerRepository = RecognizerRepository(Database.getInstance().recognizerDao)
        return RecognizerViewModelFactory(recognizerRepository)
    }

    fun provideStorageViewModelFactory(): StorageViewModelFactory {
        val storageRepository = StorageRepository.getInstance(Database.getInstance().storageDao)
        return StorageViewModelFactory(storageRepository)
    }

}