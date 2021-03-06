package com.neverim.talkinghistory.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.models.adapters.EdgeArrayAdapter
import com.neverim.talkinghistory.data.models.Edge
import com.neverim.talkinghistory.data.models.Vertex
import com.neverim.talkinghistory.ui.viewmodels.DialogueViewModel
import com.neverim.talkinghistory.ui.viewmodels.RecognizerViewModel
import com.neverim.talkinghistory.utilities.InjectorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class DialogueActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var listView: ListView
    private lateinit var btnRestart: Button
    private lateinit var btnSpeak: Button

    private lateinit var edgeAdapter: EdgeArrayAdapter
    private lateinit var currentQuestion: Vertex

    private var edgeArray = ArrayList<Edge>()
    private var selectedChar: String? = null

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

        edgeAdapter = EdgeArrayAdapter(this, edgeArray)
        listView.adapter = edgeAdapter
        edgeAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener { parent, view, position, id ->
            val edges = dialogueViewModel.edgesWithoutUiUpdate(currentQuestion)
            val element: Edge = listView.adapter?.getItem(position) as Edge


            for (edge in edges) {
                if (edge.destination.data == element.destination.data) {
                    val dstVertexEdges =
                        dialogueViewModel.edgesWithoutUiUpdate(edge.destination)
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
            if (!recognizerViewModel.isRecording()) {
                Toast.makeText(this, "Recording.", Toast.LENGTH_SHORT).show()
                recognizerViewModel.recordAudio()
            } else {
                recognizerViewModel.stopAudio()
                Toast.makeText(this, "Stopped.", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.IO).launch {
                    val answer = recognizerViewModel.getTranscript()
                    withContext(Dispatchers.Default) {
                        for (i in 0 until listView.adapter?.count!!) {
                            val item = listView.adapter?.getItem(i) as Edge
                            val entry = item.destination.data.toLowerCase(Locale.forLanguageTag("lt-LT"))
                            val lowerCaseAnswer = answer?.toLowerCase(Locale.forLanguageTag("lt-LT"))

                            if (lowerCaseAnswer?.contains(entry) == true) {
                                //listView.getChildAt(i).setBackgroundColor(Color.GREEN)
                                val dstVertexEdges = dialogueViewModel.edgesWithoutUiUpdate(item.destination)
                                makeToast("Pasirinktas atsakymas: $entry")
                                for (dstEdge in dstVertexEdges) {
                                    if (item.source.data == textView.text) {
                                        setText(dstEdge.destination.data)
                                        currentQuestion = dstEdge.destination
                                        dialogueViewModel.edges(currentQuestion)
                                        break
                                    }
                                }
                                break
                            }
                            else {
                                if (lowerCaseAnswer != null) {
                                    makeToast(lowerCaseAnswer)
                                }
                            }
                        }
                    }
                }
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
}
