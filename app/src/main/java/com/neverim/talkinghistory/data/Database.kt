package com.neverim.talkinghistory.data

class Database private constructor() {

    var adjacenciesDao = AdjacenciesDao()
        private set

    companion object {
        @Volatile private var instance: Database? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: Database().also { instance = it }
            }
    }

}