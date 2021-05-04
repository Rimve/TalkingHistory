package com.neverim.talkinghistory.data

class Database private constructor() {

    var characterDao: CharacterDao = CharacterDao()
        private set

    var recognizerDao: RecognizerDao = RecognizerDao()
        private set

    var storageDao: StorageDao = StorageDao()
        private set

    companion object {
        @Volatile private var instance: Database? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: Database().also { instance = it }
            }
    }

}