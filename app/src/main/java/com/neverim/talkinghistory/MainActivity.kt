package com.neverim.talkinghistory

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.neverim.talkinghistory.adapters.EdgeArrayAdapter
import com.neverim.talkinghistory.models.AdjacencyList
import com.neverim.talkinghistory.models.Edge
import com.neverim.talkinghistory.models.NodeEntry
import com.neverim.talkinghistory.models.Vertex


class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var listView: ListView
    private lateinit var restartBtn: Button

    private lateinit var edgeAdapter: EdgeArrayAdapter
    private lateinit var currentQuestion: Vertex
    private lateinit var mRootRef: FirebaseDatabase
    private lateinit var mNodesRef: DatabaseReference
    private lateinit var mAdjacenciesRef: DatabaseReference

    private var edgeArray: ArrayList<Edge>? = ArrayList()
    private var graph = AdjacencyList()
    private var verticies: ArrayList<Vertex> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        listView = findViewById(R.id.listView)
        restartBtn = findViewById(R.id.restart_btn)

        mRootRef = FirebaseDatabase.getInstance()
        mNodesRef = mRootRef.getReference("nodes")
        mAdjacenciesRef = mRootRef.getReference("adjacencies")

        fillGraph()

        listView.setOnItemClickListener { parent, view, position, id ->
            val edges = graph.edges(currentQuestion)
            val element: Edge = listView.adapter?.getItem(position) as Edge
            for (edge in edges) {
                if (edge.destination.data.entry == element.destination.data.entry) {
                    val dstVertexEdges = graph.edges(edge.destination)
                    for (dstEdge in dstVertexEdges) {
                        if (edge.source.data.entry == textView.text) {
                            changeQuestions(view, dstEdge)
                        }
                    }
                }
            }
        }

        restartBtn.setOnClickListener {
            fillGraph()
        }
    }

    private fun changeQuestions(v: View, edge: Edge) {
        textView.text = edge.destination.data.entry
        currentQuestion = edge.destination
        edgeArray?.clear()
        edgeArray?.addAll(graph.edges(currentQuestion))
        edgeAdapter.notifyDataSetChanged()
    }

    private fun fillGraph() {
        edgeArray?.clear()
        graph.clear()
        verticies.clear()

        val adjacenciesPostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                getAdjacenciesFromDatabase(dataSnapshot.value as HashMap<String, ArrayList<ArrayList<Long>>>)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Firebase", "loadPost:onCancelled", databaseError.toException())
            }
        }

        val nodesPostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                getNodesFromDatabase(dataSnapshot.value as HashMap<String, ArrayList<String>>)
                mAdjacenciesRef.addValueEventListener(adjacenciesPostListener)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("Firebase", "loadPost:onCancelled", databaseError.toException())
            }
        }

        mNodesRef.addValueEventListener(nodesPostListener)
//        println(graph)
//        graph.updateDB()
    }

    private fun getNodesFromDatabase(nodes: HashMap<String, ArrayList<String>>) {
        for (node in nodes) {
            for (value in node.value) {
                verticies.add(graph.createVertex(NodeEntry(node.value.indexOf(value), node.key, value), false))
            }
        }
    }

    private fun getAdjacenciesFromDatabase(nodes: HashMap<String, ArrayList<ArrayList<Long>>>) {
        for (node in nodes) {
            for (i in node.value.indices) {
                if (node.value[i] != null) {
                    val srcVertex = verticies[i]
                    for (dstNode in node.value[i]) {
                        val dstVertex = verticies[dstNode.toInt()]
                        graph.addDirectedEdge(srcVertex, dstVertex)
                    }
                }
            }
        }

        currentQuestion = verticies[0]
        edgeArray?.addAll(graph.edges(currentQuestion))
        edgeAdapter = edgeArray?.let { EdgeArrayAdapter(this, it) }!!

        listView.adapter = edgeAdapter
        textView.text = currentQuestion.data.entry
    }
}
