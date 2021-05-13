package com.neverim.talkinghistory.data

import android.util.Log
import com.google.firebase.database.*
import com.neverim.talkinghistory.utilities.Constants


class CharacterRepository constructor(private val characterDao: CharacterDao) {

    private val LOG_TAG = this.javaClass.simpleName

    private val databaseHelper: DatabaseSource = DatabaseSource()
    private var vertices: HashMap<String, Vertex> = HashMap()

    private fun addFile(nodeId: Int, charName: String, fileName: String) {
        characterDao.addFile(nodeId, charName, fileName)
    }

    private fun addErrorFile(nodeId: Int, fileName: String) {
        characterDao.addErrorFile(nodeId, fileName)
    }

    fun createVertex(index: Int, node: String): Vertex {
        return characterDao.createVertex(index, node)
    }

    private fun clearFileList() {
        characterDao.clearFileList()
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

    fun getQuestions() = characterDao.getQuestions()

    fun retrieveFirst() : Vertex? = characterDao.retrieveFirst()

    fun clear() = characterDao.clear()

    fun edges(source: Vertex) = characterDao.edges(source)

    fun edgesWithoutUi(source: Vertex) = characterDao.edgesWithoutUi(source)

    fun fetchCharDataFromDb(charName: String) {
        databaseHelper.getNodesRef().keepSynced(true)
        databaseHelper.getAdjacencyRef().keepSynced(true)
        databaseHelper.getFilesLocRef().keepSynced(true)
        databaseHelper.getSimilaritiesRef().keepSynced(true)
        getNodes(charName)
        getAdjacencies(charName)
    }

    // Uncategorized word insertion
    fun insertUncategorizedWord(charName: String, node: Vertex, word: String) {
        var index = 0
        databaseHelper.getUndefinedRef()
            .child(charName)
            .child(node.index.toString()).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.value != null) {
                        val amountOfUncats = task.result?.value as ArrayList<*>
                        index = amountOfUncats.size
                    }
                    databaseHelper.getUndefinedRef()
                        .child(charName)
                        .child(node.index.toString())
                        .child(index.toString())
                        .setValue(word)
                        .addOnCompleteListener { uncatTask ->
                            Log.i(LOG_TAG, "adding uncategorized word to database")
                            if (uncatTask.isSuccessful) {
                                Log.i(LOG_TAG, "added uncategorized word to database")
                            } else {
                                Log.e(LOG_TAG, uncatTask.exception.toString())
                            }
                        }
                } else {
                    Log.e(LOG_TAG, task.exception.toString())
                }
            }
    }

    fun fetchCharListFromDb(callbackI: IDatabaseCallback) {
        databaseHelper.getNodesRef().get().addOnCompleteListener { task ->
            Log.i(LOG_TAG, "getting all available chars from database")
            val response = DatabaseResponse()
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
            callbackI.onResponse(response)
        }
    }

    fun getDescription(callbackI: IDatabaseCallback, charName: String) {
        databaseHelper.getFilesLocRef().child(charName).child("description").get().addOnCompleteListener { task ->
            Log.i(LOG_TAG, "getting description for '$charName'")
            val response = DatabaseResponse()
            if (task.isSuccessful) {
                val result = task.result
                result?.value?.let { response.data = result.value as String }
            }
            else {
                Log.e(LOG_TAG, task.toString())
                response.exception = task.exception
            }
            callbackI.onResponse(response)
        }
    }

    fun getSimilarities(callbackI: IDatabaseCallback) {
        databaseHelper.getSimilaritiesRef().get().addOnCompleteListener { task ->
            Log.i(LOG_TAG, "getting word similarities")
            val response = DatabaseResponse()
            if (task.isSuccessful) {
                val result = task.result
                result?.value?.let { response.data = result.value as HashMap<String, ArrayList<String>> }
            } else {
                Log.e(LOG_TAG, task.exception.toString())
                response.exception = task.exception
            }
            callbackI.onResponse(response)
        }
    }

    fun getImageFileName(callbackI: IDatabaseCallback, charName: String) {
        databaseHelper.getFilesLocRef().child(charName).child("image").get().addOnCompleteListener { task ->
            Log.i(LOG_TAG, "getting image fileName for '$charName'")
            val response = DatabaseResponse()
            if (task.isSuccessful) {
                val result = task.result
                result?.value?.let { response.data = result.value as String }
            }
            else {
                Log.e(LOG_TAG, task.toString())
                response.exception = task.exception
            }
            callbackI.onResponse(response)
        }
    }

    fun getAudioFileList(callbackI: IDatabaseCallback, charName: String) {
        databaseHelper.getFilesLocRef().child(charName).child("audio").get().addOnCompleteListener { task ->
            Log.i(LOG_TAG, "getting audio file list for '$charName'")
            val response = DatabaseResponse()
            if (task.isSuccessful) {
                val result = task.result
                result?.value?.let {
                    getFileListFromDatabase(result, charName)
                    response.data = characterDao.getAudioFileList()
                }
            }
            else {
                Log.e(LOG_TAG, task.toString())
                response.data = null
                response.exception = task.exception
            }
            callbackI.onResponse(response)
        }
    }

    fun getErrorAudioFileList(callbackI: IDatabaseCallback) {
        databaseHelper.getFilesLocRef().child(Constants.ERROR).child("audio").get()
            .addOnCompleteListener { task ->
                Log.i(LOG_TAG, "getting audio file list for '${Constants.ERROR}'")
                val response = DatabaseResponse()
                if (task.isSuccessful) {
                    val result = task.result
                    result?.value?.let {
                        getFileListFromDatabase(result, Constants.ERROR)
                        response.data = characterDao.getErrorAudioFileList()
                    }
                }
                else {
                    Log.e(LOG_TAG, task.toString())
                    response.data = null
                    response.exception = task.exception
                }
                callbackI.onResponse(response)
            }
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

    fun getFileListFromDatabase(snapshot: DataSnapshot, charName: String) {
        Log.i(LOG_TAG, "getting file list from database")
        clearFileList()

        if (snapshot.value is ArrayList<*>) {
            val files = snapshot.value as ArrayList<String?>
            files.forEachIndexed { nodeId, data ->
                if (data != null) {
                    if (charName == Constants.ERROR) {
                        addErrorFile(nodeId, data)
                    }
                    else {
                        addFile(nodeId, charName, data)
                    }
                }
            }
        }

        if (snapshot.value is HashMap<*, *>) {
            val files = snapshot.value as HashMap<String, String>
            for ((key, value) in files) {
                if (charName == Constants.ERROR) {
                    addErrorFile(key.toInt(), value)
                }
                else {
                    addFile(key.toInt(), charName, value)
                }
            }
        }
    }

    fun getNodesFromDatabase(snapshot: DataSnapshot) {
        Log.i(LOG_TAG, "getting vertexes from database")
        if (snapshot.value is ArrayList<*>) {
            val nodes = snapshot.value as ArrayList<String?>
            nodes.forEachIndexed { index, data ->
                if (data != null) {
                    vertices[index.toString()] = createVertex(index, data)
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

    fun getAdjacenciesFromDatabase(dataSnapshot: DataSnapshot) {
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
            val adjacencies = dataSnapshot.value as ArrayList<*>
            for (key in adjacencies.indices) {
                if (adjacencies[key] != null) {
                    if (adjacencies[key] is ArrayList<*>) {
                        val srcVertex = vertices[key.toString()]
                        for (dstNode in adjacencies[key]!! as ArrayList<String>) {
                            if (dstNode != null) {
                                val dstVertex = vertices[dstNode]
                                addDirectedEdge(srcVertex!!, dstVertex!!)
                            }
                        }
                    }
                    if (adjacencies[key] is HashMap<*, *>) {
                        val srcVertex = vertices[key.toString()]
                        for ((key, value) in adjacencies[key]!! as HashMap<String, String>) {
                            val dstVertex = vertices[value]
                            addDirectedEdge(srcVertex!!, dstVertex!!)
                        }
                    }
                }
            }
        }
    }

//    companion object {
//        @Volatile private var instance: CharacterRepository? = null
//
//        fun getInstance(adjacenciesDao: CharacterDao) =
//            instance ?: synchronized(this) {
//                instance ?: CharacterRepository(adjacenciesDao).also { instance = it }
//            }
//    }

}