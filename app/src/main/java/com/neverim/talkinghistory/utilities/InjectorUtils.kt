package com.neverim.talkinghistory.utilities

import com.neverim.talkinghistory.data.Database
import com.neverim.talkinghistory.data.repositories.AdjacenciesRepository
import com.neverim.talkinghistory.ui.viewmodels.DialogueViewModelFactory

object InjectorUtils {

    fun provideAdjacenciesViewModelFactory(charName: String) : DialogueViewModelFactory {
        val adjacenciesRepository = AdjacenciesRepository.getInstance(Database.getInstance().adjacenciesDao, charName)
        return DialogueViewModelFactory(adjacenciesRepository)
    }

}