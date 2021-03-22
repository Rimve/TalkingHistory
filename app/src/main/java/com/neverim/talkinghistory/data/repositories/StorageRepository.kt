package com.neverim.talkinghistory.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.storage.StorageReference
import com.neverim.talkinghistory.data.StorageSource
import com.neverim.talkinghistory.data.daos.StorageDao
import com.neverim.talkinghistory.data.models.FileLoc
import java.io.File

class StorageRepository private constructor(
    private val storageDao: StorageDao,
    private val charName: String) {

    private val LOG_TAG = this.javaClass.simpleName

    private val storageHelper = StorageSource()
    private var mAudioFilesRef: StorageReference = storageHelper.audioStorageRef(charName)
    private var mImageRef: StorageReference = storageHelper.imageStorageRef(charName)

    private fun getAudioFile(nodeFile: FileLoc) {
        val localFile = File.createTempFile(nodeFile.fileName, "mp3")
        val downloadTask = mAudioFilesRef.child("${nodeFile.fileName}.mp3").getFile(localFile)
        downloadTask.addOnFailureListener {
            Log.i(LOG_TAG, "audio download task failed")
        }.addOnSuccessListener {
            Log.i(LOG_TAG, "audio download completed")
            storageDao.setAudioFile(localFile)
        }
    }

    private fun getImageFile(fileName: String) {
        val localFile = File.createTempFile(fileName, "jpg")
        val downloadTask = mImageRef.child("$fileName.jpg").getFile(localFile)
        downloadTask.addOnFailureListener {
            Log.i(LOG_TAG, "image download task failed")
        }.addOnSuccessListener {
            Log.i(LOG_TAG, "image download completed")
            storageDao.setImageFile(localFile)
        }
    }

    // Let's fetch the files
    fun fetchAudioFile(nodeFile: FileLoc) = getAudioFile(nodeFile)

    fun fetchImageFile(fileName: String) = getImageFile(fileName)

    // Return the set live data that is being observed by view
    fun getAudio() = storageDao.getAudio()

    fun getImage() = storageDao.getImage()

    companion object {
        @Volatile private var instance: StorageRepository? = null

        fun getInstance(storageDao: StorageDao, charName: String) =
            instance ?: synchronized(this) {
                instance ?: StorageRepository(storageDao, charName).also { instance = it }
            }
    }
}