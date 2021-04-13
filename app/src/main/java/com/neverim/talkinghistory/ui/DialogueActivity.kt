package com.neverim.talkinghistory.ui

import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.DatabaseCallback
import com.neverim.talkinghistory.data.models.*
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
import kotlin.collections.HashMap


class DialogueActivity : AppCompatActivity() {

    private val LOG_TAG = this.javaClass.simpleName

    // View elements and models
    private lateinit var textView: TextView
    private lateinit var listeningCircle: ImageView
    private lateinit var micImage: ImageView
    private lateinit var speakImage: ImageView
    private lateinit var edgeAdapter: EdgeArrayAdapter
    private lateinit var currentQuestion: Vertex
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var progressBar: ProgressBar

    // ViewModels
    private lateinit var characterViewModel: CharacterViewModel
    private lateinit var storageViewModel: StorageViewModel
    private lateinit var recognizerViewModel: RecognizerViewModel

    // ViewModel factories
    private val characterFactory = InjectorUtils.provideCharacterViewModelFactory()
    private val storageFactory = InjectorUtils.provideStorageViewModelFactory()
    private val recognizerFactory = InjectorUtils.provideRecognizerViewModelFactory()

    // Variables / values
    private lateinit var scaleDown: Animation
    private lateinit var scaleIn: Animation
    private var similaritiesMap = HashMap<String, ArrayList<String>>()
    private var edgeArray = ArrayList<Edge>()
    private var fileList = ArrayList<FileLoc>()
    private var errorFileList = ArrayList<FileLoc>()
    private var selectedChar: String? = null
    private var answer: String? = null
    private var errorCount = 0
    private var connectionLost = false
    private var endOfDialogue = false
    private var initialized = false
    private val locale = Locale.forLanguageTag(Constants.LANGUAGE_CODEC)
    private val animationSet = AnimationSet(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogue)

        textView = findViewById(R.id.tv_dial_question)
        listeningCircle = findViewById(R.id.iv_dial_listening_circle)
        progressBar = findViewById(R.id.pb_dialogue)
        micImage = findViewById(R.id.iv_dial_mic)
        speakImage = findViewById(R.id.iv_dial_speak)

        selectedChar = intent.getStringExtra("char")

        HelperUtils.checkPermissions(this)

        if (selectedChar != null) {
            initializeUi(selectedChar!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.Default).launch {
            stopActivity()
        }
        Log.e(LOG_TAG, "on destroy")
    }

//    override fun onStop() {
//        super.onStop()
//        stopActivity()
//        Log.e(LOG_TAG, "on stopped")
//    }
//
//    override fun onRestart() {
//        super.onRestart()
//        stopActivity()
//        if (selectedChar != null) {
//            initializeUi(selectedChar!!)
//        }
//        Log.e(LOG_TAG, "on restart")
//    }
//
//    override fun onResume() {
//        super.onResume()
//        stopActivity()
//        if (selectedChar != null) {
//            initializeUi(selectedChar!!)
//        }
//        Log.e(LOG_TAG, "on resume")
//    }

    private fun handleLoaderVisibility() {
        progressBar.visibility = View.INVISIBLE
        textView.visibility = View.VISIBLE
        listeningCircle.visibility = View.VISIBLE
        micImage.visibility = View.VISIBLE
        speakImage.visibility = View.VISIBLE
    }

    private fun initializeUi(charName: String) {
        Log.i(LOG_TAG, "initializing UI")
        characterViewModel = ViewModelProvider(this, characterFactory).get(CharacterViewModel::class.java)
        recognizerViewModel = ViewModelProvider(this, recognizerFactory).get(RecognizerViewModel::class.java)
        storageViewModel = ViewModelProvider(this, storageFactory).get(StorageViewModel::class.java)

        registerConnectionCallback()
        getErrorAudioFileList()
        getAudioFileList(charName)
        getWordSimilarities()

        recognizerViewModel.audioSetup(this)

        characterViewModel.fetchCharDataFromDb(charName)

        initiateObservers()
        initializeAnimations()

        edgeAdapter = EdgeArrayAdapter(this, edgeArray)
        edgeAdapter.notifyDataSetChanged()
        mediaPlayer = MediaPlayer()

        CoroutineScope(Dispatchers.Default).launch {
            circleScaleDown()
        }
    }

    private fun startRecognition(charName: String) {
        recognizerViewModel.startRecognition(this)
        CoroutineScope(Dispatchers.Default).launch {
            Log.i(LOG_TAG, "starting routine scope")
            while (recognizerViewModel.isRecognizing()) {
                if (answer != null) {
                    Log.e(LOG_TAG, "answer: $answer")
                    handleDialogue(charName)
                }
            }
        }
    }

    private fun stopRecognition() {
        Log.i(LOG_TAG, "stopRecognition() called")
        recognizerViewModel.stopRecognition()
    }

    private suspend fun handleDialogue(charName: String) {
        // Finding the most relevant edge according to the answer using Levenshtein distance
        val mostRelevantEdge = findEdgeWithLowestScore(answer!!.toLowerCase(locale))
        if (mostRelevantEdge != null) {
            Log.i(LOG_TAG, "found most suitable answer: $answer")
            // Stop recognition after answer to bypass 1 minute stream limit
            stopRecognition()
            // Change dialogue entry according to the most relevant answer
            changeQuestion(mostRelevantEdge, characterViewModel.edgesWithoutUiUpdate(mostRelevantEdge.destination))
            // Play the audio file attached to this question
            playCurrentQuestion(charName)
            // Change the edges according to the question so the observer would update UI
            characterViewModel.edges(currentQuestion)
            answer = null
            // If there are no more edges - the dialogue is over
            if (checkIfEnd()) {
                Log.e(LOG_TAG, "stopped recognition")
                stopActivity()
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
        Log.i(LOG_TAG, "transcript: $transcript")
        // Find what was the answer
        if (transcript == Constants.ERROR.toLowerCase(locale)) {
            return edgeArray[0]
        }
        Log.i(LOG_TAG, "calculating distance")
        val suitableNextEdge = transcript?.let { getSimilaritiesOfWord(it) }
        if (suitableNextEdge != null) {
            errorCount = 0
            return suitableNextEdge
        }
        // Otherwise no similar answer was found - user must repeat
        else {
            if (transcript != null) {
                characterViewModel.insertUncategorizedWord(selectedChar!!, currentQuestion, transcript)
            }
            playErrorAudio()
            answer = null
            return null
        }
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
        //makeToast("Pasirinktas atsakymas: ${edge.destination.data}")
        Log.i(LOG_TAG, "Pasirinktas atsakymas: ${edge.destination.data}")
        if (dstVertexEdges.size > 0) {
            currentQuestion = changeEdges(dstVertexEdges, edge)!!
        }
    }

    private fun checkIfEnd(): Boolean {
        return characterViewModel.edgesWithoutUiUpdate(currentQuestion).size == 0
    }

    private fun getAudioFileList(charName: String) {
        characterViewModel.getAudioFileList(object : DatabaseCallback {
            override fun onResponse(response: IDatabaseResponse) {
                if (response.data != null) {
                    fileList = response.data as ArrayList<FileLoc>
                    playCurrentQuestion(selectedChar!!)
                }
            }
        }, charName)
    }

    private fun getErrorAudioFileList() {
        Log.i(LOG_TAG, "getting error audio file list")
        characterViewModel.getErrorAudioFileList(object : DatabaseCallback {
            override fun onResponse(response: IDatabaseResponse) {
                if (response.data != null) {
                    errorFileList = response.data as ArrayList<FileLoc>
                }
            }
        })
    }

    private fun initiateObservers() {
        characterViewModel.getAdjacencies().observe(this, Observer { hashMap ->
            if (hashMap.size > 0) {
                currentQuestion = characterViewModel.retrieveFirst()!!
                textView.text = currentQuestion.data
                characterViewModel.edges(currentQuestion)
                if (!initialized) {
                    handleLoaderVisibility()
                    initialized = true
                }
            }
        })

        characterViewModel.getEdges().observe(this, Observer { listOfEdges ->
            edgeArray.clear()
            edgeArray.addAll(listOfEdges)
            edgeAdapter.notifyDataSetChanged()
        })

        recognizerViewModel.getTranscript().observe(this, Observer { transcript ->
            answer = transcript
        })

        storageViewModel.getAudio().observe(this, Observer { audioFile ->
            if (audioFile != null) {
                Log.i(LOG_TAG, "audio observer - file changed")
                stopRecognition()
                playAudioFile(audioFile)
                if (checkIfEnd()) {
                    storageViewModel.getAudio().removeObservers(this@DialogueActivity)
                }
            }
        })
    }

    private suspend fun stopActivity() {
        withContext(Dispatchers.Main) {
            endOfDialogue = true
            edgeArray.clear()
            recognizerViewModel.stopRecognition()
            characterViewModel.getAdjacencies().removeObservers(this@DialogueActivity)
            characterViewModel.getEdges().removeObservers(this@DialogueActivity)
            recognizerViewModel.getTranscript().removeObservers(this@DialogueActivity)
        }
    }

    private fun playAudioFile(audioFile: File) {
        try {
            Log.i(LOG_TAG, "playing audio file")
            audioFile.deleteOnExit()
            val fis = FileInputStream(audioFile)
            mediaPlayer.reset()
            mediaPlayer.setDataSource(fis.fd)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                fis.close()
                startRecognition(selectedChar!!)
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, e.toString())
        }
    }

    private fun playCurrentQuestion(charName: String) {
        getFileLocByVertex(currentQuestion)?.let { fileLoc ->  storageViewModel.getAudioFile(charName, fileLoc) }
    }

    private fun getFileLocByVertex(node: Vertex): FileLoc? {
        fileList.forEach { fileLoc ->
            if (fileLoc.nodeId == node.index) {
                return fileLoc
            }
        }
        return null
    }

    private fun getWordSimilarities() {
        Log.i(LOG_TAG, "getting similar words HashMap")
        characterViewModel.getSimilarities(object : DatabaseCallback {
            override fun onResponse(response: IDatabaseResponse) {
                if (response.data != null) {
                    similaritiesMap = response.data as HashMap<String, ArrayList<String>>
                }
            }
        })
    }

    private suspend fun getSimilaritiesOfWord(word: String): Edge? {

        var lowestDst = Constants.MAXIMUM_LEV_DISTANCE
        var possibleAnswer: String? = null
        var possibleEdge: Edge? = null

        // Check if any of the question edges has this or very similar answer
        edgeArray.forEach { edge ->
            // If expected answer is any word, return next edge
            if (edge.destination.data.toLowerCase(locale) == Constants.WORD_ANY) {
                answer = null
                return edge
            }
            // Else find the best answer through similar words in that category
            else {
                val myKey = edge.destination.data
                similaritiesMap[myKey]?.forEach { similarWord ->
                    // Calculate the minimum distance between transcript and similar words
                    // of available categories
                    val dst = HelperUtils.levDistance(similarWord.toLowerCase(locale), word.toLowerCase(locale))
                    // If we found lower scored word - it will be our answer
                    if (lowestDst > dst) {
                        lowestDst = dst
                        possibleAnswer = myKey.toLowerCase(locale)
                        possibleEdge = edge
                        Log.i(LOG_TAG, "score: $dst")
                    }
                }

                // Check if it belongs to the specific word category "Kiti"
                similaritiesMap[Constants.WORD_OTHER]?.forEach { similarWord ->
                    if (myKey == similarWord) {
                        val dst = HelperUtils.levDistance(similarWord.toLowerCase(locale), word.toLowerCase(locale))
                        if (lowestDst > dst) {
                            lowestDst = dst
                            possibleAnswer = myKey.toLowerCase(locale)
                            possibleEdge = edge
                            Log.i(LOG_TAG, "score: $dst")
                        }
                    }
                }

                if (lowestDst == 0) {
                    Log.i(LOG_TAG, "possible answer: $possibleAnswer")
                    return possibleEdge
                }
            }
        }

        if (possibleEdge != null) {
            // Return the most relevant edge and reset the answer
            Log.i(LOG_TAG, "possible answer: $possibleAnswer")
            answer = null
            return possibleEdge
        }

        return null
    }

    private suspend fun playErrorAudio() {
        Log.i(LOG_TAG, "playing error audio")
        storageViewModel.getAudioFile(Constants.ERROR, errorFileList[errorCount])
        if (errorCount == 3) {
            answer = Constants.ERROR
            handleDialogue(Constants.ERROR)
            errorCount = 0
        }
        if (errorCount < 4)
            errorCount++
    }

    private fun initializeAnimations() {
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down)
        scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)

        animationSet.addAnimation(scaleIn)
        animationSet.addAnimation(scaleDown)
    }

    private suspend fun circleScaleDown() {
        withContext(Dispatchers.Default) {
            listeningCircle.startAnimation(animationSet)
        }
    }

    private fun registerConnectionCallback() {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (connectionLost) {
                    connectionLost = false
                    startRecognition(selectedChar!!)
                }
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                Log.w(LOG_TAG, "losing connection")
                recognizerViewModel.stopRecognition()
            }

            override fun onLost(network: Network) {
                Log.e(LOG_TAG, "connection lost")
                recognizerViewModel.stopRecognition()
                connectionLost = true
            }
        })
    }

}
