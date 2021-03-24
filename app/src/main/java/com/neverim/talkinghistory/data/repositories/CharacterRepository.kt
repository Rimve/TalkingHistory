package com.neverim.talkinghistory.data.repositories

import android.util.Log
import com.google.firebase.database.*
import com.neverim.talkinghistory.data.DatabaseCallback
import com.neverim.talkinghistory.data.daos.CharacterDao
import com.neverim.talkinghistory.data.DatabaseSource
import com.neverim.talkinghistory.data.IDatabaseResponse
import com.neverim.talkinghistory.data.models.Vertex


class CharacterRepository private constructor(private val characterDao: CharacterDao) {

    private val LOG_TAG = this.javaClass.simpleName

    private val databaseHelper = DatabaseSource()
    private var vertices: HashMap<String, Vertex> = HashMap()

    fun addFile(nodeId: Int, charName: String, fileName: String) {
        characterDao.addFile(nodeId, charName, fileName)
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

    fun getAudioFileList() = characterDao.getAudioFileList()

    fun getQuestions() = characterDao.getQuestions()

    fun retrieveFirst() : Vertex? = characterDao.retrieveFirst()

    fun clear() = characterDao.clear()

    fun edges(source: Vertex) = characterDao.edges(source)

    fun edgesWithoutUi(source: Vertex) = characterDao.edgesWithoutUi(source)

    fun fetchCharDataFromDb(charName: String) {
        databaseHelper.getNodesRef().keepSynced(true)
        databaseHelper.getAdjacencyRef().keepSynced(true)
        databaseHelper.getFilesLocRef().keepSynced(true)
        getNodes(charName)
        getAdjacencies(charName)
        getAudioFileList(charName)
    }

    fun fetchCharListFromDb(callback: DatabaseCallback) {
        databaseHelper.getNodesRef().get().addOnCompleteListener { task ->
            Log.i(LOG_TAG, "getting all available chars from database")
            val response = IDatabaseResponse()
            if (task.isSuccessful) {
                val result = task.result
                result?.value?.let {
                    for (entry in result.value as HashMap<String, ArrayList<String>>) {
                        characterDao.addChar(entry.key)
                    }
                    response.data = characterDao.getCharList()
                }
            }
            else {
                Log.e(LOG_TAG, task.exception.toString())
                response.exception = task.exception
            }
            callback.onResponse(response)
        }
    }

    fun getImageFileName(charName: String, callback: DatabaseCallback) {
        databaseHelper.getFilesLocRef().child(charName).child("image").get().addOnCompleteListener { task ->
            Log.i(LOG_TAG, "getting image fileName for '$charName'")
            val response = IDatabaseResponse()
            if (task.isSuccessful) {
                val result = task.result
                result?.value?.let { response.data = result.value as String }
            }
            else {
                Log.e(LOG_TAG, task.toString())
                response.exception = task.exception
            }
            callback.onResponse(response)
        }
    }

    private fun getAudioFileList(charName: String) {
        databaseHelper.getFilesLocRef().child(charName).child("audio").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                getFileListFromDatabase(dataSnapshot, charName)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    private fun getNodes(charName: String) {
        databaseHelper.getNodesRef().child(charName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                getNodesFromDatabase(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(LOG_TAG, "nodesListener load:onCancelled", databaseError.toException())
            }
        })
    }

    private fun getAdjacencies(charName: String) {
        databaseHelper.getAdjacencyRef().child(charName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                getAdjacenciesFromDatabase(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(LOG_TAG, "adjacenciesListener load:onCancelled", databaseError.toException())
            }
        })
    }

    private fun getFileListFromDatabase(snapshot: DataSnapshot, charName: String) {
        Log.i(LOG_TAG, "getting file list from database")
        if (snapshot.value is ArrayList<*>) {
            val files = snapshot.value as ArrayList<String?>
            files.forEachIndexed { nodeId, data ->
                if (data != null) {
                    addFile(nodeId, charName, data)
                }
            }
        }

        if (snapshot.value is HashMap<*, *>) {
            val files = snapshot.value as HashMap<String, String>
            for ((key, value) in files) {
                addFile(key.toInt(), charName, value)
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

    companion object {
        @Volatile private var instance: CharacterRepository? = null

        fun getInstance(adjacenciesDao: CharacterDao) =
            instance ?: synchronized(this) {
                instance ?: CharacterRepository(adjacenciesDao).also { instance = it }
            }
    }

}