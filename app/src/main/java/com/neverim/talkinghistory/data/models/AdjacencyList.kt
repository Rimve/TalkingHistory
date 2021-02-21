package com.neverim.talkinghistory.data.models

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdjacencyList {

    private val adjacencies: HashMap<Vertex, ArrayList<Edge>> = HashMap()
    private var mRootRef: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var mNodesRef: DatabaseReference = mRootRef.getReference("nodes")
    private var mAdjacencyRef: DatabaseReference = mRootRef.getReference("adjacencies")

    fun createVertex(data: NodeEntry, addToDb: Boolean): Vertex {
        //data.index = adjacencies.count()
        val index = data.index!!
        val vertex = Vertex(index, data)
        if (addToDb) {
            data.charName?.let {
                mNodesRef.child(it).child(data.index.toString()).setValue(data.entry)
            }
        }
        adjacencies[vertex] = ArrayList()
        return vertex
    }

    override fun toString(): String {
        return buildString {
            adjacencies.forEach { (vertex, edges) ->
                val edgeString = edges.joinToString { it.destination.data.toString() }
                append("${vertex.data} ---> [ $edgeString ]\n")
            }
        }
    }

    fun addDirectedEdge(source: Vertex, destination: Vertex) {
        val edge = Edge(source, destination)
        adjacencies[source]?.add(edge)
    }

    fun addUndirectedEdge(source: Vertex, destination: Vertex) {
        addDirectedEdge(source, destination)
        addDirectedEdge(destination, source)
    }

    fun edges(source: Vertex) = adjacencies[source]?: arrayListOf()

    fun clear() {
        adjacencies.clear()
    }

    fun updateDB() {
        for (entry in adjacencies) {
            entry.key.data.charName?.let {
                for (value in entry.value) {
                    mAdjacencyRef.child(it)
                        .child(entry.key.index.toString())
                        .child(entry.value.indexOf(value).toString())
                        .setValue(value.destination.index)
                }
                //mAdjacencyRef.child(it).child(entry.key.index.toString()).setValue(entry.value)
            }
        }
    }
}