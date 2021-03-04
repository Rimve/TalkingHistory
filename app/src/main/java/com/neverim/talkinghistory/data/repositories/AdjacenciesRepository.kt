package com.neverim.talkinghistory.data.repositories

import android.util.Log
import com.google.firebase.database.*
import com.neverim.talkinghistory.data.AdjacenciesDao
import com.neverim.talkinghistory.data.FirebaseSource
import com.neverim.talkinghistory.data.models.Vertex


class AdjacenciesRepository private constructor(
    private val adjacenciesDao: AdjacenciesDao,
    private val charName: String
) {

    private val databaseHelper = FirebaseSource()
    private var verticies: HashMap<String, Vertex> = HashMap()
    private var mNodesRef: DatabaseReference = databaseHelper.getNodesRef().child(charName)
    private var mAdjacenciesRef: DatabaseReference = databaseHelper.getAdjacencyRef().child(charName)

    fun createVertex(index: Int, node: String): Vertex {
        return adjacenciesDao.createVertex(index, node)
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
            getNodesFromDatabase(dataSnapshot)
            mAdjacenciesRef.addValueEventListener(adjacenciesPostListener)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("Adjacencies Repository", "loadPost:onCancelled", databaseError.toException())
        }
    }

    private val adjacenciesPostListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.value is HashMap<*, *>) {
                getAdjacenciesFromDatabase(dataSnapshot.value as HashMap<String, ArrayList<String>>)
            }
            if (dataSnapshot.getValue() is ArrayList<*>) {
                getAdjacenciesFromDatabase(dataSnapshot.value as ArrayList<ArrayList<Long>>)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("Adjacencies Repository", "loadPost:onCancelled", databaseError.toException())
        }
    }

    private fun getAdjacenciesFromDatabase(nodes: ArrayList<ArrayList<Long>>) {
        for (key in nodes.indices) {
            if (nodes[key] != null) {
                val srcVertex = verticies[key.toString()]
                for (dstNode in nodes[key]) {
                    if (dstNode != null) {
                        val dstVertex = verticies[dstNode.toString()]
                        addDirectedEdge(srcVertex!!, dstVertex!!)
                    }
                }
            }
        }
    }

    private fun getAdjacenciesFromDatabase(nodes: HashMap<String, ArrayList<String>>) {
        for ((key, value) in nodes) {
            if (nodes[key] != null) {
                val srcVertex = verticies[key]
                for (dstNode in value) {
                    if (dstNode != null) {
                        val dstVertex = verticies[dstNode]
                        addDirectedEdge(srcVertex!!, dstVertex!!)
                    }
                }
            }
        }
    }

    private fun getNodesFromDatabase(snapshot: DataSnapshot) {
        if (snapshot.value is ArrayList<*>) {
            val nodes = snapshot.value as ArrayList<String>
            for (node in nodes) {
                verticies[nodes.indexOf(node).toString()] =
                    createVertex(nodes.indexOf(node), node)
            }
        }

        if (snapshot.value is HashMap<*, *>) {
            val nodes = snapshot.value as HashMap<String, String>
            for ((key, value) in nodes) {
                verticies[key] = createVertex(key.toInt(), value)
            }
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