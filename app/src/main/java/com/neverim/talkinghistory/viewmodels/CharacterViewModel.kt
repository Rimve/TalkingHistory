package com.neverim.talkinghistory.viewmodels

import androidx.lifecycle.ViewModel
import com.neverim.talkinghistory.data.IDatabaseCallback
import com.neverim.talkinghistory.data.Vertex
import com.neverim.talkinghistory.data.CharacterRepository

class CharacterViewModel(private val characterRepository: CharacterRepository) : ViewModel() {

    // Adjacenies methods

    fun getAdjacencies() = characterRepository.getAdjacencies()
    fun getEdges() = characterRepository.getEdges()
    fun getAudioFileList(callbackI: IDatabaseCallback, charName: String) = characterRepository.getAudioFileList(callbackI, charName)
    fun getErrorAudioFileList(callbackI: IDatabaseCallback) = characterRepository.getErrorAudioFileList(callbackI)
    fun getImageFileName(callbackI: IDatabaseCallback, charName: String) = characterRepository.getImageFileName(callbackI, charName)
    fun getDescription(callbackI: IDatabaseCallback, charName: String) = characterRepository.getDescription(callbackI, charName)
    fun getSimilarities(callbackI: IDatabaseCallback) = characterRepository.getSimilarities(callbackI)
    fun fetchCharDataFromDb(charName: String) = characterRepository.fetchCharDataFromDb(charName)
    fun insertUncategorizedWord(charName: String, node: Vertex, word: String) = characterRepository.insertUncategorizedWord(charName, node, word)
    fun fetchCharListFromDb(callbackI: IDatabaseCallback) = characterRepository.fetchCharListFromDb(callbackI)
    fun createVertex(index: Int, data: String) = characterRepository.createVertex(index, data)
    fun addDirectedEdge(source: Vertex, destination: Vertex) = characterRepository.addDirectedEdge(source, destination)
    fun clear() = characterRepository.clear()
    fun edges(source: Vertex) = characterRepository.edges(source)
    fun edgesWithoutUiUpdate(source: Vertex) = characterRepository.edgesWithoutUi(source)
    fun getQuestions() = characterRepository.getQuestions()
    fun retrieveFirst(): Vertex? = characterRepository.retrieveFirst()

}