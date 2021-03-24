package com.neverim.talkinghistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.neverim.talkinghistory.data.DatabaseCallback
import com.neverim.talkinghistory.data.models.FileLoc
import com.neverim.talkinghistory.data.repositories.StorageRepository

class StorageViewModel(private val storageRepository: StorageRepository): ViewModel() {

    // Storage methods

    fun getAudio() = storageRepository.getAudio()
    fun fetchAudio(charName: String, fileNode: FileLoc) = storageRepository.fetchAudioFile(charName, fileNode)
    fun getImageFile(callback: DatabaseCallback, charName: String, fileName: String) =
        storageRepository.getImageFile(callback, charName, fileName)
}