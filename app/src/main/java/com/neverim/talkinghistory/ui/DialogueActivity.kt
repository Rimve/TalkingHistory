package com.neverim.talkinghistory.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
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
    private lateinit var edgeAdapter: EdgeArrayAdapter
    private lateinit var currentQuestion: Vertex
    private lateinit var mediaPlayer: MediaPlayer

    // ViewModels
    private lateinit var characterViewModel: CharacterViewModel
    private lateinit var storageViewModel: StorageViewModel
    private lateinit var recognizerViewModel: RecognizerViewModel

    // ViewModel factories
    private val characterFactory = InjectorUtils.provideCharacterViewModelFactory()
    private val storageFactory = InjectorUtils.provideStorageViewModelFactory()
    private val recognizerFactory = InjectorUtils.provideRecognizerViewModelFactory()

    // Variables / values
    private var similaritiesMap = HashMap<String, ArrayList<String>>()
    private var edgeArray = ArrayList<Edge>()
    private var fileList = ArrayList<FileLoc>()
    private var selectedChar: String? = null
    private var answer: String? = null
    private var spoke = false
    private val locale = Locale.forLanguageTag(Constants.LANGUAGE_CODEC)
    private val animationSet = AnimationSet(false)
    private lateinit var scaleDown: Animation
    private lateinit var scaleIn: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogue)

        textView = findViewById(R.id.tv_dial_question)
        listeningCircle = findViewById(R.id.iv_dial_listening_circle)

        selectedChar = intent.getStringExtra("char")

        HelperUtils.checkPermissions(this)

        if (selectedChar != null) {
            initializeUi(selectedChar!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun initializeUi(charName: String) {
        Log.i(LOG_TAG, "initializing UI")
        characterViewModel = ViewModelProvider(this, characterFactory).get(CharacterViewModel::class.java)
        recognizerViewModel = ViewModelProvider(this, recognizerFactory).get(RecognizerViewModel::class.java)
        storageViewModel = ViewModelProvider(this, storageFactory).get(StorageViewModel::class.java)

        initializeAnimations()
        getAudioFileList(charName)
        getWordSimilarities()

        recognizerViewModel.audioSetup(this)

        characterViewModel.fetchCharDataFromDb(charName)

        initiateObservers()

        edgeAdapter = EdgeArrayAdapter(this, edgeArray)
        edgeAdapter.notifyDataSetChanged()
        mediaPlayer = MediaPlayer()

        CoroutineScope(Dispatchers.Default).launch {
            circleScaleDown()
        }

        recognizerViewModel.startRecognition(this)
        CoroutineScope(Dispatchers.IO).launch {
            while (recognizerViewModel.isRecognizing()) {
                if (answer != null) {
                    // Finding the most relevant edge according to the answer using Levenshtein distance
                    val mostRelevantEdge = findEdgeWithLowestScore(answer!!.toLowerCase(locale))
                    if (mostRelevantEdge != null) {
                        Log.i(LOG_TAG, "found most suitable answer: $answer")
                        // Stop recognition after answer to bypass 1 minute stream limit
                        recognizerViewModel.stopRecognition()
                        // Change dialogue entry according to the most relevant answer
                        changeQuestion(mostRelevantEdge, characterViewModel.edgesWithoutUiUpdate(mostRelevantEdge.destination))
                        // Play the audio file attached to this question
                        playCurrentQuestion(charName)
                        // Change the edges according to the question so the observer would update UI
                        characterViewModel.edges(currentQuestion)
                        answer = null
                        // If there are no more edges - the dialogue is over
                        if (edgeArray.size == 0) {
                            Log.e(LOG_TAG, "stopping recognition")
                            mediaPlayer.release()
                        }
                        // Otherwise start the recognition again (bypassing 1 minute limit)
                        else {
                            recognizerViewModel.startRecognition(this@DialogueActivity)
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

    private suspend fun findEdgeWithLowestScore(transcript: String?): Edge? {
        Log.i(LOG_TAG, "calculating distance")
        // Find what was the answer
        val possibleAnswer = transcript?.let { getSimilaritiesOfWord(it) }
        if (transcript != null) {
            // Check if any of the question edges has this or very similar answer
            edgeArray.forEach { edge ->
                // If answer can be any word then we do not need to search further
                if (edge.destination.data.toLowerCase(locale) == Constants.WORD_ANY) {
                    answer = null
                    return edge
                }
                if (possibleAnswer != null && edge.destination.data.toLowerCase(locale) == possibleAnswer) {
                    // Return the most relevant edge and reset the answer
                    answer = null
                    return edge
                }
            }
            characterViewModel.insertUncategorizedWord(selectedChar!!, currentQuestion, transcript)
        }
        // Otherwise no similar answer was found - user must repeat
        else {
            answer = null
            return null
        }
        answer = null
        return null
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

    private fun getAudioFileList(charName: String) {
        characterViewModel.getAudioFileList(object : DatabaseCallback {
            override fun onResponse(response: IDatabaseResponse) {
                if (response.data != null) {
                    fileList = response.data as ArrayList<FileLoc>
                }
            }
        }, charName)
    }

    private fun initiateObservers() {
        characterViewModel.getAdjacencies().observe(this, Observer { hashMap ->
            if (hashMap.size > 0) {
                currentQuestion = characterViewModel.retrieveFirst()!!
                textView.text = currentQuestion.data
                characterViewModel.edges(currentQuestion)
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
                //playAudioFile(audioFile)
            }
        })
    }

    private fun playAudioFile(audioFile: File) {
        try {
            audioFile.deleteOnExit()
            val fis = FileInputStream(audioFile)
            mediaPlayer.setDataSource(fis.fd)
            mediaPlayer.prepare()
            mediaPlayer.start()
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

    private suspend fun getSimilaritiesOfWord(word: String): String? {

        var lowestDst = Constants.MAXIMUM_LEV_DISTANCE
        var possibleAnswer: String? = null

        similaritiesMap.forEach { (keyWord, similarities) ->
            if (similarities is ArrayList<*>) {
                similarities.forEach { similarWord ->
                    // Calculate the minimum distance between words
                    val dst = HelperUtils.levDistance(
                        similarWord.toLowerCase(locale),
                        word.toLowerCase(locale)
                    )
                    // If we found lower scored word - it will be our answer
                    if (lowestDst > dst) {
                        lowestDst = dst
                        possibleAnswer = keyWord.toLowerCase(locale)
                    }
                    // If that word is not positive answer nor is negative
                    // then we assume it might be a specific word if it is of sufficient distance score
                    if (keyWord == Constants.WORD_OTHER && dst < Constants.MAXIMUM_LEV_DISTANCE) {
                        possibleAnswer = similarWord.toLowerCase(locale)
                    }
                }
            }
        }
        return possibleAnswer
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

}
