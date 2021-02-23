package com.neverim.talkinghistory.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.models.adapters.EdgeArrayAdapter
import com.neverim.talkinghistory.data.models.Edge
import com.neverim.talkinghistory.data.models.Vertex
import com.neverim.talkinghistory.ui.viewmodels.DialogueViewModel
import com.neverim.talkinghistory.utilities.InjectorUtils


class DialogueActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var listView: ListView
    private lateinit var restartBtn: Button

    private lateinit var edgeAdapter: EdgeArrayAdapter
    private lateinit var currentQuestion: Vertex

    private var edgeArray = ArrayList<Edge>()
    private var selectedChar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogue)

        textView = findViewById(R.id.textView)
        listView = findViewById(R.id.listView)
        restartBtn = findViewById(R.id.btn_restart)

        selectedChar = intent.getStringExtra("char")

        initializeUi(selectedChar!!)
    }

    private fun initializeUi(charName: String) {
        val factory = InjectorUtils.provideAdjacenciesViewModelFactory(charName)
        val viewModel = ViewModelProvider(this, factory).get(DialogueViewModel::class.java)

        viewModel.getAdjacencies().observe(this, Observer {
            if (it.size > 0) {
                currentQuestion = viewModel.retrieveFirst()!!
                textView.text = currentQuestion.data.entry
                viewModel.edges(currentQuestion)
            }
        })

        viewModel.getEdges().observe(this, Observer {
            edgeArray.clear()
            edgeArray.addAll(it)
            edgeAdapter.notifyDataSetChanged()
        })

        edgeAdapter = EdgeArrayAdapter(this, edgeArray)
        listView.adapter = edgeAdapter
        edgeAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener { parent, view, position, id ->
            val edges = viewModel.edgesWithoutUiUpdate(currentQuestion)
            val element: Edge = listView.adapter?.getItem(position) as Edge

            for (edge in edges) {
                if (edge.destination.data.entry == element.destination.data.entry) {
                    val dstVertexEdges = viewModel.edgesWithoutUiUpdate(edge.destination)
                    for (dstEdge in dstVertexEdges) {
                        if (edge.source.data.entry == textView.text) {
                            textView.text = dstEdge.destination.data.entry
                            currentQuestion = dstEdge.destination
                            viewModel.edges(currentQuestion)
                        }
                    }
                }
            }
        }

        restartBtn.setOnClickListener {
            initializeUi(selectedChar!!)
        }
    }
}
