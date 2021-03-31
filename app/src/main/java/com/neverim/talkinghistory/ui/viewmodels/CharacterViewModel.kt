package com.neverim.talkinghistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.neverim.talkinghistory.data.DatabaseCallback
import com.neverim.talkinghistory.data.models.Vertex
import com.neverim.talkinghistory.data.repositories.CharacterRepository

class CharacterViewModel(private val characterRepository: CharacterRepository) : ViewModel() {

    // Adjacenies methods

    fun getAdjacencies() = characterRepository.getAdjacencies()
    fun getEdges() = characterRepository.getEdges()
    fun getAudioFileList(callback: DatabaseCallback, charName: String) = characterRepository.getAudioFileList(callback, charName)
    fun getImageFileName(callback: DatabaseCallback, charName: String) = characterRepository.getImageFileName(callback, charName)
    fun fetchCharDataFromDb(charName: String) = characterRepository.fetchCharDataFromDb(charName)
    fun fetchCharListFromDb(callback: DatabaseCallback) = characterRepository.fetchCharListFromDb(callback)
    fun createVertex(index: Int, data: String) = characterRepository.createVertex(index, data)
    fun addDirectedEdge(source: Vertex, destination: Vertex) = characterRepository.addDirectedEdge(source, destination)
    fun addUndirectedEdge(source: Vertex, destination: Vertex) = characterRepository.addUndirectedEdge(source, destination)
    fun clear() = characterRepository.clear()
    fun edges(source: Vertex) = characterRepository.edges(source)
    fun edgesWithoutUiUpdate(source: Vertex) = characterRepository.edgesWithoutUi(source)
    fun getQuestions() = characterRepository.getQuestions()
    fun retrieveFirst(): Vertex? = characterRepository.retrieveFirst()

}