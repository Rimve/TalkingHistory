package com.neverim.talkinghistory.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseSource {

    companion object {
        @Volatile private var instance: FirebaseDatabase? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FirebaseDatabase.getInstance().also {
                    instance = it
                }
            }
    }

    fun getNodesRef() = instance?.getReference("nodes") ?: getInstance().getReference("nodes")

    fun getAdjacencyRef() = instance?.getReference("adjacencies") ?: getInstance().getReference("adjacencies")

}