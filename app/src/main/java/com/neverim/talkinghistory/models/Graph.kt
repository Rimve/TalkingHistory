package com.neverim.talkinghistory.models

interface Graph<T> {
    fun createVertex(data: T): Vertex<T>

    fun addDirectedEdge(source: Vertex<T>, destination: Vertex<T>)

    fun addUndirectedEdge(source: Vertex<T>, destination: Vertex<T>)

    fun edges(source: Vertex<T>): ArrayList<Edge<T>>
}