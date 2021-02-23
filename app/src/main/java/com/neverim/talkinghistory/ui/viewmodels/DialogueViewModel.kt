package com.neverim.talkinghistory.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.neverim.talkinghistory.data.models.NodeEntry
import com.neverim.talkinghistory.data.models.Vertex
import com.neverim.talkinghistory.data.repositories.AdjacenciesRepository

class DialogueViewModel(private val adjacenciesRepository: AdjacenciesRepository) : ViewModel() {

    fun getAdjacencies() = adjacenciesRepository.getAdjacencies()
    fun getEdges() = adjacenciesRepository.getEdges()
    private fun fetchFromDatabase() = adjacenciesRepository.fetchFromDatabase()
    fun createVertex(node: NodeEntry) = adjacenciesRepository.createVertex(node)
    fun addDirectedEdge(source: Vertex, destination: Vertex) = adjacenciesRepository.addDirectedEdge(source, destination)
    fun addUndirectedEdge(source: Vertex, destination: Vertex) = adjacenciesRepository.addUndirectedEdge(source, destination)
    fun clear() = adjacenciesRepository.clear()
    fun edges(source: Vertex) = adjacenciesRepository.edges(source)
    fun edgesWithoutUiUpdate(source: Vertex) = adjacenciesRepository.edgesWithoutUi(source)
    fun getQuestions() = adjacenciesRepository.getQuestions()
    fun getFirst() = adjacenciesRepository.getFirst()
    fun retrieveFirst() : Vertex? = adjacenciesRepository.retrieveFirst()

    init {
        fetchFromDatabase()
    }

}