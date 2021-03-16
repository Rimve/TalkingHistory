package com.neverim.talkinghistory.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.models.Edge
import com.neverim.talkinghistory.data.models.Vertex
import com.neverim.talkinghistory.data.models.adapters.EdgeArrayAdapter
import com.neverim.talkinghistory.ui.viewmodels.DialogueViewModel
import com.neverim.talkinghistory.ui.viewmodels.RecognizerViewModel
import com.neverim.talkinghistory.utilities.Constants
import com.neverim.talkinghistory.utilities.HelperUtils
import com.neverim.talkinghistory.utilities.InjectorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class DialogueActivity : AppCompatActivity() {

    private val LOG_TAG = this.javaClass.simpleName

    private lateinit var textView: TextView
    private lateinit var listView: ListView
    private lateinit var btnRestart: Button
    private lateinit var btnSpeak: Button

    private lateinit var edgeAdapter: EdgeArrayAdapter
    private lateinit var currentQuestion: Vertex

    private var edgeArray = ArrayList<Edge>()
    private var selectedChar: String? = null
    private var answer: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogue)

        textView = findViewById(R.id.tv_dialogue_question)
        listView = findViewById(R.id.lv_dialogue_choices)
        btnRestart = findViewById(R.id.btn_dialogue_restart)
        btnSpeak = findViewById(R.id.btn_dialogue_speak)

        selectedChar = intent.getStringExtra("char")

        initializeUi(selectedChar!!)
    }

    private fun initializeUi(charName: String) {
        Log.i(LOG_TAG, "initializing UI")
        val adjacenciesFactory = InjectorUtils.provideAdjacenciesViewModelFactory(charName)
        val recognizerFactory = InjectorUtils.provideRecognizerViewModelFactory(this)
        val dialogueViewModel = ViewModelProvider(this, adjacenciesFactory).get(DialogueViewModel::class.java)
        val recognizerViewModel = ViewModelProvider(this, recognizerFactory).get(RecognizerViewModel::class.java)

        recognizerViewModel.audioSetup()

        dialogueViewModel.getAdjacencies().observe(this, Observer {
            if (it.size > 0) {
                currentQuestion = dialogueViewModel.retrieveFirst()!!
                textView.text = currentQuestion.data
                dialogueViewModel.edges(currentQuestion)
            }
        })

        dialogueViewModel.getEdges().observe(this, Observer {
            edgeArray.clear()
            edgeArray.addAll(it)
            edgeAdapter.notifyDataSetChanged()
        })

        recognizerViewModel.getTranscript().observe(this, Observer {
            answer = it
        })

        edgeAdapter = EdgeArrayAdapter(this, edgeArray)
        listView.adapter = edgeAdapter
        edgeAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener { parent, view, position, id ->
            val edges = dialogueViewModel.edgesWithoutUiUpdate(currentQuestion)
            val element: Edge = listView.adapter?.getItem(position) as Edge

            for (edge in edges) {
                if (edge.destination.data == element.destination.data) {
                    val dstVertexEdges = dialogueViewModel.edgesWithoutUiUpdate(edge.destination)
                    for (dstEdge in dstVertexEdges) {
                        if (edge.source.data == textView.text) {
                            textView.text = dstEdge.destination.data
                            currentQuestion = dstEdge.destination
                            dialogueViewModel.edges(currentQuestion)
                        }
                    }
                }
            }
        }

        btnRestart.setOnClickListener {
            initializeUi(selectedChar!!)
        }

        btnSpeak.setOnClickListener {
            if (!recognizerViewModel.isRecognizing()) {
                Toast.makeText(this, "Recognizing..", Toast.LENGTH_SHORT).show()
                recognizerViewModel.startRecognition()
                CoroutineScope(Dispatchers.IO).launch {
                    while (recognizerViewModel.isRecognizing()) {
                        if (answer != null) {
                            // TODO: implement end of dialogue - atm a person can speak when there is no more dialogue
                            val mostRelevantEdge = findEdgeWithLowestScore(answer!!.toLowerCase(Locale.forLanguageTag(Constants.LANGUAGE_CODEC)))
                            if (mostRelevantEdge != null) {
                                Log.i(LOG_TAG, "found most suitable answer: $answer")
                                changeQuestion(mostRelevantEdge,
                                    dialogueViewModel.edgesWithoutUiUpdate(mostRelevantEdge.destination))
                                dialogueViewModel.edges(currentQuestion)
                                answer = null
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show()
                recognizerViewModel.stopRecognition()
            }
        }
    }

    private suspend fun setText(text: String) {
        withContext(Dispatchers.Main) {
            textView.text = text
        }
    }

    private suspend fun makeToast(text: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@DialogueActivity, text, Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun findEdgeWithLowestScore(transcript: String?): Edge? {
        Log.i(LOG_TAG, "calculating distance")
        var lowestDst = Int.MAX_VALUE
        var mostRelevantEdge: Edge? = null
        if (transcript != null) {
            edgeArray.forEach {
                val dst = HelperUtils.levDistance(transcript,
                    it.destination.data.toLowerCase(Locale.forLanguageTag(Constants.LANGUAGE_CODEC)))
                if (lowestDst > dst) {
                    lowestDst = dst
                    mostRelevantEdge = it
                }
            }
        }
        answer = null
        return mostRelevantEdge
    }

    private suspend fun changeEdges(dstNodeEdges: ArrayList<Edge>, srcEdge: Edge): Vertex? {
        for (dstEdge in dstNodeEdges) {
            if (srcEdge.source.data == textView.text) {
                setText(dstEdge.destination.data)
                return dstEdge.destination
            }
        }
        return null
    }

    private suspend fun changeQuestion(edge: Edge, dstVertexEdges: ArrayList<Edge>) {
        makeToast("Pasirinktas atsakymas: ${edge.destination.data}")
        if (dstVertexEdges.size > 0) {
            currentQuestion = changeEdges(dstVertexEdges, edge)!!
        }
    }

}
