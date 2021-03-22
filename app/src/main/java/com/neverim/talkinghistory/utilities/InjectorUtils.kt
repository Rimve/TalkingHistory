package com.neverim.talkinghistory.utilities

import android.content.Context
import com.neverim.talkinghistory.data.Database
import com.neverim.talkinghistory.data.repositories.CharacterRepository
import com.neverim.talkinghistory.data.repositories.RecognizerRepository
import com.neverim.talkinghistory.data.repositories.StorageRepository
import com.neverim.talkinghistory.ui.viewmodels.DialogueViewModelFactory
import com.neverim.talkinghistory.ui.viewmodels.RecognizerViewModelFactory

object InjectorUtils {

    fun provideAdjacenciesViewModelFactory(charName: String) : DialogueViewModelFactory {
        val adjacenciesRepository = CharacterRepository.getInstance(Database.getInstance().characterDao, charName)
        val storageRepository = StorageRepository.getInstance(Database.getInstance().storageDao, charName)
        return DialogueViewModelFactory(adjacenciesRepository, storageRepository)
    }

    fun provideRecognizerViewModelFactory(context: Context) : RecognizerViewModelFactory {
        val recognizerRepository = RecognizerRepository(Database.getInstance().recognizerDao)
        return RecognizerViewModelFactory(recognizerRepository, context)
    }

}