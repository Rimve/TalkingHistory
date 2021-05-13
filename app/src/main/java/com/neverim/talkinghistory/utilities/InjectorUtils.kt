package com.neverim.talkinghistory.utilities

import com.neverim.talkinghistory.data.Database
import com.neverim.talkinghistory.data.CharacterRepository
import com.neverim.talkinghistory.data.RecognizerRepository
import com.neverim.talkinghistory.data.StorageRepository
import com.neverim.talkinghistory.viewmodels.CharacterViewModelFactory
import com.neverim.talkinghistory.viewmodels.RecognizerViewModelFactory
import com.neverim.talkinghistory.viewmodels.StorageViewModelFactory

object InjectorUtils {

    fun provideCharacterViewModelFactory(): CharacterViewModelFactory {
        val adjacenciesRepository = CharacterRepository(Database.getInstance().characterDao)
        return CharacterViewModelFactory(adjacenciesRepository)
    }

    fun provideRecognizerViewModelFactory(): RecognizerViewModelFactory {
        val recognizerRepository = RecognizerRepository(Database.getInstance().recognizerDao)
        return RecognizerViewModelFactory(recognizerRepository)
    }

    fun provideStorageViewModelFactory(): StorageViewModelFactory {
        val storageRepository = StorageRepository(Database.getInstance().storageDao)
        return StorageViewModelFactory(storageRepository)
    }

}