package com.neverim.talkinghistory.data

import com.google.firebase.database.FirebaseDatabase

class DatabaseSource {

    companion object {
        @Volatile private var instance: FirebaseDatabase? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FirebaseDatabase.getInstance().also {
                    instance = it
                }
            }
    }

    private fun getFirebaseRef() = instance ?: getInstance()

    fun getNodesRef() = getFirebaseRef().getReference("nodes")

    fun getAdjacencyRef() = getFirebaseRef().getReference("adjacencies")

    fun getFilesLocRef() = getFirebaseRef().getReference("files")

    fun getSimilaritiesRef() = getFirebaseRef().getReference("similarities")

    fun getUndefinedRef() = getFirebaseRef().getReference("undefined")

}