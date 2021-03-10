package com.neverim.talkinghistory.utilities

import android.content.Context
import com.neverim.talkinghistory.data.Database
import com.neverim.talkinghistory.data.daos.RecognizerDao
import com.neverim.talkinghistory.data.repositories.AdjacenciesRepository
import com.neverim.talkinghistory.data.repositories.RecognizerRepository
import com.neverim.talkinghistory.ui.viewmodels.DialogueViewModelFactory
import com.neverim.talkinghistory.ui.viewmodels.RecognizerViewModelFactory

object InjectorUtils {

    fun provideAdjacenciesViewModelFactory(charName: String) : DialogueViewModelFactory {
        val adjacenciesRepository = AdjacenciesRepository.getInstance(Database.getInstance().adjacenciesDao, charName)
        return DialogueViewModelFactory(adjacenciesRepository)
    }

    fun provideRecognizerViewModelFactory(context: Context) : RecognizerViewModelFactory {
        val recognizerRepository = RecognizerRepository(RecognizerDao())
        return RecognizerViewModelFactory(recognizerRepository, context)
    }

}