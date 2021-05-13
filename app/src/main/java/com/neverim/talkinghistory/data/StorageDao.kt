package com.neverim.talkinghistory.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File

class StorageDao {

    private val LOG_TAG = this.javaClass.simpleName

    private val mutableImageFile = MutableLiveData<File>()
    private val mutableAudioFile = MutableLiveData<File>()

    init {
        mutableImageFile.value = null
        mutableAudioFile.value = null
    }

    fun setImageFile(file: File) {
        mutableImageFile.value = file
    }

    fun setAudioFile(file: File) {
        mutableAudioFile.value = file
    }

    fun clear() {
        mutableImageFile.value = null
        mutableAudioFile.value = null
    }

    fun getImage() = mutableImageFile as LiveData<File>
    fun getAudio() = mutableAudioFile as LiveData<File>
}