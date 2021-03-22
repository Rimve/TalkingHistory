package com.neverim.talkinghistory.data

import com.google.firebase.storage.FirebaseStorage


class StorageSource {

    companion object {
        @Volatile private var instance: FirebaseStorage? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FirebaseStorage.getInstance().also {
                    instance = it
                }
            }
    }

    private fun getStorageRef() = instance?.reference ?: getInstance().reference

    fun imageStorageRef(charName: String) = getStorageRef().child("$charName/")

    fun audioStorageRef(charName: String) = getStorageRef().child("$charName/audio/")


}