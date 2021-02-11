package com.neverim.talkinghistory

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.neverim.talkinghistory.adapters.EdgeArrayAdapter
import com.neverim.talkinghistory.models.AdjacencyList
import com.neverim.talkinghistory.models.Edge
import com.neverim.talkinghistory.models.NodeEntry
import com.neverim.talkinghistory.models.Vertex


class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var listView: ListView

    private lateinit var edgeAdapter: EdgeArrayAdapter
    private lateinit var currentQuestion: Vertex<NodeEntry>
    private var edgeArray: ArrayList<Edge<NodeEntry>>? = ArrayList()
    private var graph = AdjacencyList<NodeEntry>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        listView = findViewById(R.id.listView)

        fillGraph()

        listView.setOnItemClickListener { parent, view, position, id ->
            val edges = graph.edges(currentQuestion)
            val element: Edge<NodeEntry> = listView.adapter?.getItem(position) as Edge<NodeEntry>
            for (edge in edges) {
                if (edge.destination.data.entry == element.destination.data.entry) {
                    val dstVertexEdeges = graph.edges(edge.destination)
                    for (dstEdge in dstVertexEdeges) {
                        if (edge.source.data.entry == textView.text) {
                            changeQuestions(view, dstEdge)
                        }
                    }
                }
            }
            println("Selected element: $element")
        }
    }

    private fun changeQuestions(v: View, edge: Edge<NodeEntry>) {
        textView.text = edge.destination.data.entry
        currentQuestion = edge.destination
        edgeArray?.clear()
        edgeArray?.addAll(graph.edges(currentQuestion))
        edgeAdapter.notifyDataSetChanged()
    }

    private fun fillGraph() {
        val questionOne = graph.createVertex(NodeEntry(-1,"Hello! What would you like to know about me?"))
        val oneAnswerOne = graph.createVertex(NodeEntry(-1,"Hello!"))
        val oneAnswerTwo = graph.createVertex(NodeEntry(-1,"Who are you?"))
        val oneAnswerThree = graph.createVertex(NodeEntry(-1,"Why are you here?"))

        val questionTwo = graph.createVertex(NodeEntry(-1,"My name is Testy and I'm here to help you!"))
        val twoAnswerOne = graph.createVertex(NodeEntry(-1,"How can you help me?"))

        val questionThree = graph.createVertex(NodeEntry(-1,"I'm seeking to help to learn about me. Would you want me to tell an interesting fact?"))
        val threeAnswerOne = graph.createVertex(NodeEntry(-1,"Sure!"))
        val threeAnswerTwo = graph.createVertex(NodeEntry(-1,"Rather not."))

        val questionFour = graph.createVertex(NodeEntry(-1,"I was made in 2021-02-05!"))
        val fourAnswerOne = graph.createVertex(NodeEntry(-1,"Interesting."))
        val fourAnswerTwo = graph.createVertex(NodeEntry(-1,"Boring."))

        val byeQuestion = graph.createVertex(NodeEntry(-1,"Goodbye."))
        val byeReply = graph.createVertex(NodeEntry(-1,"Bye!"))

        currentQuestion = questionOne

        graph.addDirectedEdge(questionOne, oneAnswerOne)
        graph.addDirectedEdge(questionOne, oneAnswerTwo)
        graph.addDirectedEdge(questionOne, oneAnswerThree)
        graph.addDirectedEdge(questionOne, byeQuestion)

        graph.addDirectedEdge(byeQuestion, byeReply)

        graph.addDirectedEdge(questionTwo, twoAnswerOne)
        graph.addDirectedEdge(questionTwo, byeQuestion)

        graph.addDirectedEdge(oneAnswerTwo, questionTwo)
        graph.addDirectedEdge(oneAnswerTwo, byeReply)

        graph.addDirectedEdge(twoAnswerOne, questionThree)
        graph.addDirectedEdge(questionThree, threeAnswerOne)
        graph.addDirectedEdge(questionThree, threeAnswerTwo)
        graph.addDirectedEdge(questionThree, byeQuestion)

        graph.addDirectedEdge(threeAnswerOne, questionFour)
        graph.addDirectedEdge(questionFour, fourAnswerOne)
        graph.addDirectedEdge(questionFour, fourAnswerTwo)
        graph.addDirectedEdge(questionFour, byeQuestion)

        graph.addDirectedEdge(oneAnswerThree, questionThree)

        edgeArray?.addAll(graph.edges(questionOne))
        edgeAdapter = edgeArray?.let { EdgeArrayAdapter(this, it) }!!

        println(graph)

        listView.adapter = edgeAdapter
        textView.text = questionOne.data.entry
    }
}
