package com.neverim.talkinghistory.data.repositories

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.neverim.talkinghistory.data.AdjacenciesDao
import com.neverim.talkinghistory.data.FirebaseSource
import com.neverim.talkinghistory.data.models.NodeEntry
import com.neverim.talkinghistory.data.models.Vertex

class AdjacenciesRepository private constructor(private val adjacenciesDao: AdjacenciesDao, private val charName: String) {

    private val databaseHelper = FirebaseSource()
    private var verticies: ArrayList<Vertex> = ArrayList()
    private var mNodesRef: DatabaseReference = databaseHelper.getNodesRef().child(charName)
    private var mAdjacenciesRef: DatabaseReference = databaseHelper.getAdjacencyRef().child(charName)

    fun createVertex(node: NodeEntry): Vertex {
        return adjacenciesDao.createVertex(node)
    }

    fun addDirectedEdge(source: Vertex, destination: Vertex) {
        adjacenciesDao.addDirectedEdge(source, destination)
    }

    fun addUndirectedEdge(source: Vertex, destination: Vertex) {
        addDirectedEdge(source, destination)
        addDirectedEdge(destination, source)
    }

    fun getAdjacencies() = adjacenciesDao.getAdjacencies()

    fun getEdges() = adjacenciesDao.getEdges()

    fun getQuestions() = adjacenciesDao.getQuestions()

    fun getFirst() = adjacenciesDao.getFirst()

    fun retrieveFirst() : Vertex? = adjacenciesDao.retrieveFirst()

    fun clear() = adjacenciesDao.clear()

    fun edges(source: Vertex) = adjacenciesDao.edges(source)

    fun edgesWithoutUi(source: Vertex) = adjacenciesDao.edgesWithoutUi(source)

        private val nodesPostListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            getNodesFromDatabase(dataSnapshot.value as ArrayList<String>)
            mAdjacenciesRef.addValueEventListener(adjacenciesPostListener)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("Adjacencies Repository", "loadPost:onCancelled", databaseError.toException())
        }
    }

    private val adjacenciesPostListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            getAdjacenciesFromDatabase(dataSnapshot.value as ArrayList<ArrayList<Long>>)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("Adjacencies Repository", "loadPost:onCancelled", databaseError.toException())
        }
    }

    private fun getAdjacenciesFromDatabase(nodes: ArrayList<ArrayList<Long>>) {
        for (i in nodes.indices) {
            if (nodes[i] != null) {
                val srcVertex = verticies[i]
                for (dstNode in nodes[i]) {
                    val dstVertex = verticies[dstNode.toInt()]
                    addDirectedEdge(srcVertex, dstVertex)
                }
            }
        }
    }

    private fun getNodesFromDatabase(nodes: ArrayList<String>) {
        for (node in nodes) {
            verticies.add(createVertex(NodeEntry(nodes.indexOf(node), charName, node)))
        }
    }

    fun fetchFromDatabase() {
        mNodesRef.keepSynced(true)
        mAdjacenciesRef.keepSynced(true)
        mNodesRef.addValueEventListener(nodesPostListener)
    }

    companion object {
        @Volatile private var instance: AdjacenciesRepository? = null

        fun getInstance(adjacenciesDao: AdjacenciesDao, charName: String) =
            instance ?: synchronized(this) {
                instance ?: AdjacenciesRepository(adjacenciesDao, charName).also { instance = it }
            }
    }

}