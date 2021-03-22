package com.neverim.talkinghistory.data

import com.neverim.talkinghistory.data.daos.CharacterDao
import com.neverim.talkinghistory.data.daos.RecognizerDao
import com.neverim.talkinghistory.data.daos.StorageDao

class Database private constructor() {

    var characterDao = CharacterDao()
        private set

    var recognizerDao = RecognizerDao()
        private set

    var storageDao = StorageDao()
        private set

    companion object {
        @Volatile private var instance: Database? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: Database().also { instance = it }
            }
    }

}