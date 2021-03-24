package com.neverim.talkinghistory.data.repositories

import android.graphics.BitmapFactory
import android.util.Log
import com.neverim.talkinghistory.data.DatabaseCallback
import com.neverim.talkinghistory.data.IDatabaseResponse
import com.neverim.talkinghistory.data.StorageSource
import com.neverim.talkinghistory.data.daos.StorageDao
import com.neverim.talkinghistory.data.models.FileLoc
import java.io.File


class StorageRepository private constructor(private val storageDao: StorageDao) {

    private val LOG_TAG = this.javaClass.simpleName

    private val storageHelper = StorageSource()

    private fun getAudioFile(charName: String, nodeFile: FileLoc) {
        val localFile = File.createTempFile(nodeFile.fileName, ".mp3")
        val downloadTask = storageHelper.audioStorageRef(charName).child("${nodeFile.fileName}.mp3").getFile(
            localFile
        )
        downloadTask.addOnFailureListener {
            Log.e(LOG_TAG, "audio download task failed")
        }.addOnSuccessListener {
            Log.i(LOG_TAG, "audio download completed")
            storageDao.setAudioFile(localFile)
        }
    }

    fun getImageFile(callback: DatabaseCallback, charName: String, fileName: String) {
        val localFile = File.createTempFile(fileName, ".jpg")
        val downloadTask = storageHelper.imageStorageRef(charName).child("$fileName.jpg").getFile(
            localFile
        )
        downloadTask.addOnCompleteListener { task ->
            Log.i(LOG_TAG, "image download task completed")
            val response = IDatabaseResponse()
            if (task.isSuccessful) {
                response.data = BitmapFactory.decodeFile(localFile.path)
            }
            else {
                Log.e(LOG_TAG, task.exception.toString())
                response.exception = task.exception
            }
            callback.onResponse(response)
        }
    }

    // Let's fetch the audio file
    fun fetchAudioFile(charName: String, nodeFile: FileLoc) = getAudioFile(charName, nodeFile)

    // Return the set live data that is being observed by view
    fun getAudio() = storageDao.getAudio()


    companion object {
        @Volatile private var instance: StorageRepository? = null

        fun getInstance(storageDao: StorageDao) =
            instance ?: synchronized(this) {
                instance ?: StorageRepository(storageDao).also { instance = it }
            }
    }
}