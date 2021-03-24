package com.neverim.talkinghistory.data.daos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.neverim.talkinghistory.data.models.Edge
import com.neverim.talkinghistory.data.models.FileLoc
import com.neverim.talkinghistory.data.models.Vertex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CharacterDao {

    private val LOG_TAG = this.javaClass.simpleName

    private val adjacencies = HashMap<Vertex, ArrayList<Edge>>()
    private val filesList = ArrayList<FileLoc>()
    private val charList = ArrayList<String>()
    private val edges = ArrayList<Edge>()
    private val question = ArrayList<Vertex>()
    private var imageFileName: String = ""
    private var first: Vertex? = null

    private val mutableAdjacenciesList = MutableLiveData<HashMap<Vertex, ArrayList<Edge>>>()
    private val mutableFilesList = MutableLiveData<ArrayList<FileLoc>>()
    private val mutableEdges = MutableLiveData<ArrayList<Edge>>()
    private val mutableQuestion = MutableLiveData<ArrayList<Vertex>>()

    init {
        Log.i(LOG_TAG, "initializing DAO")
        mutableAdjacenciesList.value = adjacencies
        mutableEdges.value = edges
        mutableQuestion.value = question
        mutableFilesList.value = filesList
    }

    fun addChar(charName: String) {
        charList.add(charName)
    }

    fun addFile(nodeId: Int, charName: String, fileName: String) {
        filesList.add(FileLoc(nodeId, charName, fileName))
        mutableFilesList.value = filesList
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
        Log.i(LOG_TAG, "clearing adjacencies DAO")
        adjacencies.clear()
        edges.clear()
        question.clear()
        charList.clear()
        first = null
    }

    fun retrieveFirst(): Vertex? {
        Log.i(LOG_TAG, "searching for initial node")
        var lowestIndex = Int.MAX_VALUE
        for (adj in adjacencies) {
            if (adj.key.index < lowestIndex) {
                lowestIndex = adj.key.index
                first = adj.key
            }
        }
        return first
    }

    fun setImageFileName(name: String) {
        imageFileName = name
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
    fun getAudioFileList() = mutableFilesList as LiveData<ArrayList<FileLoc>>
    fun getCharList() = charList
    fun getImageFileName() = imageFileName

}