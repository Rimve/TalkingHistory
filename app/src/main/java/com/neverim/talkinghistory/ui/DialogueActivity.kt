package com.neverim.talkinghistory.ui

import android.media.MediaPlayer
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
import com.neverim.talkinghistory.data.models.FileLoc
import com.neverim.talkinghistory.data.models.Vertex
import com.neverim.talkinghistory.data.models.adapters.EdgeArrayAdapter
import com.neverim.talkinghistory.ui.viewmodels.CharacterViewModel
import com.neverim.talkinghistory.ui.viewmodels.RecognizerViewModel
import com.neverim.talkinghistory.ui.viewmodels.StorageViewModel
import com.neverim.talkinghistory.utilities.Constants
import com.neverim.talkinghistory.utilities.HelperUtils
import com.neverim.talkinghistory.utilities.InjectorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import javax.net.ssl.*
import kotlin.collections.ArrayList


class DialogueActivity : AppCompatActivity() {

    private val LOG_TAG = this.javaClass.simpleName

    private lateinit var textView: TextView
    private lateinit var listView: ListView
    private lateinit var btnRestart: Button

    private lateinit var edgeAdapter: EdgeArrayAdapter
    private lateinit var currentQuestion: Vertex
    private lateinit var mediaPlayer: MediaPlayer

    private var edgeArray = ArrayList<Edge>()
    private var fileList = ArrayList<FileLoc>()
    private var selectedChar: String? = null
    private var answer: String? = null
    private var spoke = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogue)

        textView = findViewById(R.id.tv_dialogue_question)
        listView = findViewById(R.id.lv_dialogue_choices)
        btnRestart = findViewById(R.id.btn_dialogue_restart)

        selectedChar = intent.getStringExtra("char")

        initializeUi(selectedChar!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun initializeUi(charName: String) {
        Log.i(LOG_TAG, "initializing UI")
        val characterFactory = InjectorUtils.provideCharacterViewModelFactory()
        val recognizerFactory = InjectorUtils.provideRecognizerViewModelFactory(this)
        val storageFactory = InjectorUtils.provideStorageViewModelFactory()
        val characterViewModel = ViewModelProvider(this, characterFactory).get(CharacterViewModel::class.java)
        val recognizerViewModel = ViewModelProvider(this, recognizerFactory).get(RecognizerViewModel::class.java)
        val storageViewModel = ViewModelProvider(this, storageFactory).get(StorageViewModel::class.java)

        characterViewModel.getAudioFileList().observe(this, Observer { fileList = it })

        recognizerViewModel.audioSetup()

        characterViewModel.fetchCharDataFromDb(charName)

        characterViewModel.getAdjacencies().observe(this, Observer { hashMap ->
            if (hashMap.size > 0) {
                currentQuestion = characterViewModel.retrieveFirst()!!
                textView.text = currentQuestion.data
                characterViewModel.edges(currentQuestion)
                if (!spoke && this::currentQuestion.isInitialized) {
                    getFileLocByVertex(currentQuestion)?.let { storageViewModel.fetchAudio(charName, it) } // Testing audio file
                    spoke = true
                }
            }
        })

        characterViewModel.getEdges().observe(this, Observer {
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
        mediaPlayer = MediaPlayer()

        storageViewModel.getAudio().observe(this, Observer {
            if (it != null) {
                playAudioFile(it)
            }
        })

        //recognizerViewModel.startRecognition()
        CoroutineScope(Dispatchers.IO).launch {
            while (recognizerViewModel.isRecognizing()) {
                if (answer != null) {
                    val mostRelevantEdge = findEdgeWithLowestScore(answer!!.toLowerCase(Locale.forLanguageTag(Constants.LANGUAGE_CODEC)))
                    if (mostRelevantEdge != null) {
                        Log.i(LOG_TAG, "found most suitable answer: $answer")
                        recognizerViewModel.stopRecognition()
                        changeQuestion(mostRelevantEdge, characterViewModel.edgesWithoutUiUpdate(mostRelevantEdge.destination))
                        getFileLocByVertex(currentQuestion)?.let { storageViewModel.fetchAudio(charName, it) }
                        characterViewModel.edges(currentQuestion)
                        answer = null
                        if (edgeArray.size != 1) {
                            recognizerViewModel.startRecognition()
                        }
                        else {
                            mediaPlayer.release()
                        }
                    }
                }
            }
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val edges = characterViewModel.edgesWithoutUiUpdate(currentQuestion)
            val element: Edge = listView.adapter?.getItem(position) as Edge

            for (edge in edges) {
                if (edge.destination.data == element.destination.data) {
                    val dstVertexEdges = characterViewModel.edgesWithoutUiUpdate(edge.destination)
                    for (dstEdge in dstVertexEdges) {
                        if (edge.source.data == textView.text) {
                            textView.text = dstEdge.destination.data
                            currentQuestion = dstEdge.destination
                            characterViewModel.edges(currentQuestion)
                        }
                    }
                }
            }
        }

        btnRestart.setOnClickListener {
            initializeUi(selectedChar!!)
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
            spoke = false
        }
    }

    private fun playAudioFile(audioFile: File) {
        try {
            audioFile.deleteOnExit()

            // Tried passing path directly, but kept getting
            // "Prepare failed.: status=0x1"
            // so using file descriptor instead
            val fis = FileInputStream(audioFile)
            mediaPlayer.setDataSource(fis.fd)
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: IOException) {
            Log.e(LOG_TAG, e.toString())
        }
    }

    private fun getFileLocByVertex(node: Vertex): FileLoc? {
        fileList.forEach {
            if (it.nodeId == node.index) {
                return it
            }
        }
        return null
    }

}
