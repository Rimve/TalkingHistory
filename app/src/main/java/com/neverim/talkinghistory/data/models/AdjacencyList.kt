package com.neverim.talkinghistory.data.models

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdjacencyList {

    private val adjacencies: HashMap<Vertex, ArrayList<Edge>> = HashMap()
    private var mRootRef: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var mNodesRef: DatabaseReference = mRootRef.getReference("nodes")
    private var mAdjacencyRef: DatabaseReference = mRootRef.getReference("adjacencies")

    fun createVertex(index: Int, data: String): Vertex {
        val vertex = Vertex(index, data)
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
}