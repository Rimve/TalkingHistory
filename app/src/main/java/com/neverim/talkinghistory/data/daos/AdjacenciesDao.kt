package com.neverim.talkinghistory.data.daos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.neverim.talkinghistory.data.models.Edge
import com.neverim.talkinghistory.data.models.Vertex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdjacenciesDao {

    private val adjacencies = HashMap<Vertex, ArrayList<Edge>>()
    private val edges = ArrayList<Edge>()
    private val question = ArrayList<Vertex>()
    private var first: Vertex? = null

    private val mutableAdjacenciesList = MutableLiveData<HashMap<Vertex, ArrayList<Edge>>>()
    private val mutableEdges = MutableLiveData<ArrayList<Edge>>()
    private val mutableQuestion = MutableLiveData<ArrayList<Vertex>>()
    private val mutableFirst = MutableLiveData<Vertex>()

    init {
        mutableAdjacenciesList.value = adjacencies
        mutableEdges.value = edges
        mutableQuestion.value = question
    }

    fun createVertex(index: Int, data: String): Vertex {
        val vertex = Vertex(index, data)
        adjacencies[vertex] = ArrayList()
        question.add(vertex)
        mutableQuestion.value = question
        return vertex
    }

    fun addDirectedEdge(source: Vertex, destination: Vertex) {
        val edge = Edge(source, destination)
        adjacencies[source]?.add(edge)
        mutableAdjacenciesList.value = adjacencies
    }

    fun clear() {
        adjacencies.clear()
        edges.clear()
        question.clear()
        first = null
    }

    fun retrieveFirst() : Vertex? {
        var lowestIndex = Int.MAX_VALUE
        for (thing in adjacencies) {
            if (thing.key.index < lowestIndex) {
                lowestIndex = thing.key.index
                first = thing.key
            }
        }
        return first
    }

    fun edges(source: Vertex): ArrayList<Edge> {
        edges.clear()
        edges.addAll(adjacencies[source]?: arrayListOf())
        mutableEdges.postValue(edges)
        return edges
    }

    fun edgesWithoutUi(source: Vertex) = adjacencies[source]?: arrayListOf()
    fun getAdjacencies() = mutableAdjacenciesList as LiveData<HashMap<Vertex, ArrayList<Edge>>>
    fun getEdges() = mutableEdges as LiveData<ArrayList<Edge>>
    fun getQuestions() = mutableQuestion as LiveData<ArrayList<Vertex>>
    fun getFirst() = mutableFirst as LiveData<Vertex>

}