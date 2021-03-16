package com.neverim.talkinghistory.data.repositories

import android.util.Log
import com.google.firebase.database.*
import com.neverim.talkinghistory.data.daos.AdjacenciesDao
import com.neverim.talkinghistory.data.FirebaseSource
import com.neverim.talkinghistory.data.models.Vertex


class AdjacenciesRepository private constructor(
    private val adjacenciesDao: AdjacenciesDao,
    private val charName: String) {

    private val LOG_TAG = this.javaClass.simpleName

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
            Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException())
        }
    }

    private val adjacenciesPostListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            getAdjacenciesFromDatabase(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException())
        }
    }

    private fun getAdjacenciesFromDatabase(dataSnapshot: DataSnapshot) {
        Log.i(LOG_TAG, "getting adjacencies from database")
        if (dataSnapshot.value is HashMap<*, *>) {
            val adjacencies = dataSnapshot.value as HashMap<String, ArrayList<String?>>
            for ((key, value) in adjacencies) {
                if (adjacencies[key] != null) {
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

        if (dataSnapshot.value is ArrayList<*>) {
            val adjacencies = dataSnapshot.value as ArrayList<ArrayList<String?>?>
            for (key in adjacencies.indices) {
                if (adjacencies[key] != null) {
                    val srcVertex = verticies[key.toString()]
                    for (dstNode in adjacencies[key]!!) {
                        if (dstNode != null) {
                            val dstVertex = verticies[dstNode]
                            addDirectedEdge(srcVertex!!, dstVertex!!)
                        }
                    }
                }
            }
        }
    }

    private fun getNodesFromDatabase(snapshot: DataSnapshot) {
        Log.i(LOG_TAG, "getting vertexes from database")
        if (snapshot.value is ArrayList<*>) {
            val nodes = snapshot.value as ArrayList<String?>
            nodes.forEachIndexed { index, data ->
                if (data != null) {
                    verticies[index.toString()] =
                        createVertex(index, data)
                }
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