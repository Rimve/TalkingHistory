package com.neverim.talkinghistory

import android.content.Intent
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


class DialogueActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var listView: ListView
    private lateinit var restartBtn: Button
    private lateinit var selectorBtn: Button

    private lateinit var edgeAdapter: EdgeArrayAdapter
    private lateinit var currentQuestion: Vertex
    private lateinit var mRootRef: FirebaseDatabase
    private lateinit var mNodesRef: DatabaseReference
    private lateinit var mAdjacenciesRef: DatabaseReference

    private var edgeArray: ArrayList<Edge>? = ArrayList()
    private var graph = AdjacencyList()
    private var verticies: ArrayList<Vertex> = ArrayList()
    private var selectedChar: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogue)

        textView = findViewById(R.id.textView)
        listView = findViewById(R.id.listView)
        restartBtn = findViewById(R.id.btn_restart)
        selectorBtn = findViewById(R.id.btn_selector)

        selectedChar = intent.getStringExtra("char")

        // Get firebase database root node
        mRootRef = FirebaseDatabase.getInstance()
        // Get subtree of nodes
        mNodesRef = mRootRef.getReference("nodes").child(selectedChar!!)
        // Store a local copy of the data
        mNodesRef.keepSynced(true)
        // Get subtree of adjacencies
        mAdjacenciesRef = mRootRef.getReference("adjacencies").child(selectedChar!!)
        // Store a local copy of the data
        mAdjacenciesRef.keepSynced(true)

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

        selectorBtn.setOnClickListener {
            val selectorIntent = Intent(this@DialogueActivity, SelectorActivity::class.java)
            startActivity(selectorIntent)
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
                getAdjacenciesFromDatabase(dataSnapshot.value as ArrayList<ArrayList<Long>>)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase", "loadPost:onCancelled", databaseError.toException())
            }
        }

        val nodesPostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                getNodesFromDatabase(dataSnapshot.value as ArrayList<String>)
                mAdjacenciesRef.addValueEventListener(adjacenciesPostListener)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase", "loadPost:onCancelled", databaseError.toException())
            }
        }

        mNodesRef.addValueEventListener(nodesPostListener)
//        println(graph)
//        graph.updateDB()
    }

    private fun getNodesFromDatabase(nodes: ArrayList<String>) {
        for (node in nodes) {
            verticies.add(graph.createVertex(NodeEntry(nodes.indexOf(node), selectedChar, node), false))
        }
    }

    private fun getAdjacenciesFromDatabase(nodes: ArrayList<ArrayList<Long>>) {
        for (i in nodes.indices) {
            if (nodes[i] != null) {
                val srcVertex = verticies[i]
                for (dstNode in nodes[i]) {
                    val dstVertex = verticies[dstNode.toInt()]
                    graph.addDirectedEdge(srcVertex, dstVertex)
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
