package com.neverim.talkinghistory.data.repositories

import android.util.Log
import com.google.firebase.database.*
import com.neverim.talkinghistory.data.daos.CharacterDao
import com.neverim.talkinghistory.data.DatabaseSource
import com.neverim.talkinghistory.data.models.Vertex


class CharacterRepository private constructor(
    private val characterDao: CharacterDao,
    private val charName: String) {

    private val LOG_TAG = this.javaClass.simpleName

    private val databaseHelper = DatabaseSource()
    private var vertices: HashMap<String, Vertex> = HashMap()
    private var mNodesRef: DatabaseReference = databaseHelper.getNodesRef().child(charName)
    private var mAdjacenciesRef: DatabaseReference = databaseHelper.getAdjacencyRef().child(charName)
    private var mAudioFilesRef: DatabaseReference = databaseHelper.getFilesLocRef().child(charName).child("audio")

    fun addFile(nodeId: Int, fileName: String) {
        characterDao.addFile(nodeId, fileName)
    }

    fun createVertex(index: Int, node: String): Vertex {
        return characterDao.createVertex(index, node)
    }

    fun addDirectedEdge(source: Vertex, destination: Vertex) {
        characterDao.addDirectedEdge(source, destination)
    }

    fun addUndirectedEdge(source: Vertex, destination: Vertex) {
        addDirectedEdge(source, destination)
        addDirectedEdge(destination, source)
    }

    fun getAdjacencies() = characterDao.getAdjacencies()

    fun getEdges() = characterDao.getEdges()

    fun getFiles() = characterDao.getFileList()

    fun getQuestions() = characterDao.getQuestions()

    fun getFirst() = characterDao.getFirst()

    fun retrieveFirst() : Vertex? = characterDao.retrieveFirst()

    fun clear() = characterDao.clear()

    fun edges(source: Vertex) = characterDao.edges(source)

    fun edgesWithoutUi(source: Vertex) = characterDao.edgesWithoutUi(source)

    private val fileListListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            getFileListFromDatabase(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException())
        }
    }

    private val nodesListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            getNodesFromDatabase(dataSnapshot)
            mAdjacenciesRef.addValueEventListener(adjacenciesListener)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(LOG_TAG, "nodesListener load:onCancelled", databaseError.toException())
        }
    }

    private val adjacenciesListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            getAdjacenciesFromDatabase(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w(LOG_TAG, "adjacenciesListener load:onCancelled", databaseError.toException())
        }
    }

    private fun getAdjacenciesFromDatabase(dataSnapshot: DataSnapshot) {
        Log.i(LOG_TAG, "getting adjacencies from database")
        if (dataSnapshot.value is HashMap<*, *>) {
            val adjacencies = dataSnapshot.value as HashMap<String, ArrayList<String?>>
            for ((key, value) in adjacencies) {
                if (adjacencies[key] != null) {
                    val srcVertex = vertices[key]
                    for (dstNode in value) {
                        if (dstNode != null) {
                            val dstVertex = vertices[dstNode]
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
                    val srcVertex = vertices[key.toString()]
                    for (dstNode in adjacencies[key]!!) {
                        if (dstNode != null) {
                            val dstVertex = vertices[dstNode]
                            addDirectedEdge(srcVertex!!, dstVertex!!)
                        }
                    }
                }
            }
        }
    }

    private fun getFileListFromDatabase(snapshot: DataSnapshot) {
        Log.i(LOG_TAG, "getting file list from database")
        if (snapshot.value is ArrayList<*>) {
            val files = snapshot.value as ArrayList<String?>
            files.forEachIndexed { nodeId, data ->
                if (data != null) {
                    addFile(nodeId, data)
                }
            }
        }

        if (snapshot.value is HashMap<*, *>) {
            val files = snapshot.value as HashMap<String, String>
            for ((key, value) in files) {
                addFile(key.toInt(), value)
            }
        }
    }

    private fun getNodesFromDatabase(snapshot: DataSnapshot) {
        Log.i(LOG_TAG, "getting vertexes from database")
        if (snapshot.value is ArrayList<*>) {
            val nodes = snapshot.value as ArrayList<String?>
            nodes.forEachIndexed { index, data ->
                if (data != null) {
                    vertices[index.toString()] =
                        createVertex(index, data)
                }
            }
        }

        if (snapshot.value is HashMap<*, *>) {
            val nodes = snapshot.value as HashMap<String, String>
            for ((key, value) in nodes) {
                vertices[key] = createVertex(key.toInt(), value)
            }
        }
    }

    fun fetchFromDatabase() {
        mNodesRef.keepSynced(true)
        mAdjacenciesRef.keepSynced(true)
        mAudioFilesRef.keepSynced(true)
        mNodesRef.addValueEventListener(nodesListener)
        mAudioFilesRef.addValueEventListener(fileListListener)
    }

    companion object {
        @Volatile private var instance: CharacterRepository? = null

        fun getInstance(adjacenciesDao: CharacterDao, charName: String) =
            instance ?: synchronized(this) {
                instance ?: CharacterRepository(adjacenciesDao, charName).also { instance = it }
            }
    }

}