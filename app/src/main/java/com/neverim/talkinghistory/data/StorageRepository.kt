package com.neverim.talkinghistory.data

import android.graphics.BitmapFactory
import android.util.Log
import java.io.File


class StorageRepository constructor(private val storageDao: StorageDao) {

    private val LOG_TAG = this.javaClass.simpleName

    private val storageHelper: StorageSource = StorageSource()

    private fun getAudioFile(charName: String, nodeFile: FileLoc) {
        Log.i(LOG_TAG, "downloading audio file for: $charName")
        val localFile = File.createTempFile(nodeFile.fileName, ".mp3")
        val downloadTask =
            storageHelper.audioStorageRef(charName).child("${nodeFile.fileName}.mp3")
                .getFile(localFile)
        downloadTask.addOnFailureListener {
            Log.e(LOG_TAG, "audio download task failed")
        }.addOnSuccessListener {
            Log.i(LOG_TAG, "audio download completed")
            storageDao.setAudioFile(localFile)
        }
    }

    fun getImageFile(callbackI: IDatabaseCallback, charName: String, fileName: String?) {
        if (fileName != null) {
            Log.i(LOG_TAG, "downloading image file for: $charName")
            val localFile = File.createTempFile(fileName, ".jpg")
            val downloadTask = storageHelper.imageStorageRef(charName)
                .child("$fileName.jpg")
                .getFile(localFile)
            downloadTask.addOnCompleteListener { task ->
                Log.i(LOG_TAG, "image download task completed")
                val response = DatabaseResponse()
                if (task.isSuccessful) {
                    response.data = BitmapFactory.decodeFile(localFile.path)
                } else {
                    Log.e(LOG_TAG, task.exception.toString())
                    response.exception = task.exception
                }
                callbackI.onResponse(response)
            }.addOnFailureListener {
                Log.e(LOG_TAG, "image download task failed with exception: $it")
            }
        }
    }

    // Let's fetch the audio file
    fun fetchAudioFile(charName: String, nodeFile: FileLoc) = getAudioFile(charName, nodeFile)

    // Return the set live data that is being observed by view
    fun getAudio() = storageDao.getAudio()

    fun clear() {
        storageDao.clear()
    }


//    companion object {
//        @Volatile private var instance: StorageRepository? = null
//
//        fun getInstance(storageDao: StorageDao) =
//            instance ?: synchronized(this) {
//                instance ?: StorageRepository(storageDao).also { instance = it }
//            }
//    }
}