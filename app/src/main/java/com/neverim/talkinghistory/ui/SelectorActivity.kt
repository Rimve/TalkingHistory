package com.neverim.talkinghistory.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.FirebaseSource


class SelectorActivity : AppCompatActivity() {

    private val LOG_TAG = this.javaClass.simpleName

    private lateinit var spinner: Spinner
    private lateinit var btnSelect: Button
    private lateinit var btnTts: Button
    private lateinit var mNodesRef: DatabaseReference
    private lateinit var adapter: ArrayAdapter<String>

    private var mRootRef = FirebaseSource()
    private var charsArray: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_char)

        spinner = findViewById(R.id.spinner_select_chars)
        btnSelect = findViewById(R.id.btn_select_char)
        btnTts = findViewById(R.id.btn_tts)

        initializeUi()
    }

    private fun initializeUi() {
        mNodesRef = mRootRef.getNodesRef()
        mNodesRef.keepSynced(true)

        val nodesPostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                getCharNamesFromDatabase(dataSnapshot.value as HashMap<String, ArrayList<String>>)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(LOG_TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        mNodesRef.addValueEventListener(nodesPostListener)

        if (spinner != null) {
            adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, charsArray)
            spinner.adapter = adapter
        }

        btnSelect.setOnClickListener {
            val dialogueIntent = Intent(this@SelectorActivity, DialogueActivity::class.java)
            dialogueIntent.putExtra("char", spinner.selectedItem.toString())
            startActivity(dialogueIntent)
        }

        btnTts.setOnClickListener {
            val ttsIntent = Intent(this@SelectorActivity, TTSActivity::class.java)
            startActivity(ttsIntent)
        }
    }

    private fun getCharNamesFromDatabase(data: HashMap<String, ArrayList<String>>) {
        Log.i(LOG_TAG, "getting all available chars from database")
        charsArray.clear()
        for (entry in data) {
            charsArray.add(entry.key)
        }
        adapter.notifyDataSetChanged()
    }
}