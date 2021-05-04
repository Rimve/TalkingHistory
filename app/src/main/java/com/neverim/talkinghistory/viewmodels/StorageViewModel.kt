package com.neverim.talkinghistory.viewmodels

import androidx.lifecycle.ViewModel
import com.neverim.talkinghistory.data.IDatabaseCallback
import com.neverim.talkinghistory.data.FileLoc
import com.neverim.talkinghistory.data.StorageRepository

class StorageViewModel(private val storageRepository: StorageRepository): ViewModel() {

    // Storage methods
    fun getAudio() = storageRepository.getAudio()
    fun getAudioFile(charName: String, fileNode: FileLoc) =
        storageRepository.fetchAudioFile(charName, fileNode)
    fun getImageFile(callbackI: IDatabaseCallback, charName: String, fileName: String?) =
        storageRepository.getImageFile(callbackI, charName, fileName)
}