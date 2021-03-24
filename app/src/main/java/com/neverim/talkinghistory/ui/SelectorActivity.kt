package com.neverim.talkinghistory.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.DatabaseCallback
import com.neverim.talkinghistory.data.IDatabaseResponse
import com.neverim.talkinghistory.data.models.CharacterInfo
import com.neverim.talkinghistory.data.models.adapters.RecyclerAdapter
import com.neverim.talkinghistory.ui.viewmodels.CharacterViewModel
import com.neverim.talkinghistory.ui.viewmodels.StorageViewModel
import com.neverim.talkinghistory.utilities.Constants
import com.neverim.talkinghistory.utilities.InjectorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SelectorActivity : AppCompatActivity() {

    private val LOG_TAG = this.javaClass.simpleName

    // View elements and adapters
    private lateinit var spinner: Spinner
    private lateinit var btnSelect: Button
    private lateinit var ivChar: ImageView
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var recyclerAdapter: RecyclerAdapter

    // ViewModels
    private lateinit var characterViewModel: CharacterViewModel
    private lateinit var storageViewModel: StorageViewModel

    // ViewModel factories
    private val characterFactory = InjectorUtils.provideCharacterViewModelFactory()
    private val storageFactory = InjectorUtils.provideStorageViewModelFactory()

    // Variables
    private var charsArray: ArrayList<String> = ArrayList()
    private var charInfoList: ArrayList<CharacterInfo> = ArrayList()
    private var backPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_char)

        spinner = findViewById(R.id.spinner_select_chars)
        btnSelect = findViewById(R.id.btn_select_char)
        ivChar = findViewById(R.id.iv_select_char)
        recycler = findViewById(R.id.rv_select_char)

        characterViewModel = ViewModelProvider(this, characterFactory).get(CharacterViewModel::class.java)
        storageViewModel = ViewModelProvider(this, storageFactory).get(StorageViewModel::class.java)
        recyclerAdapter = RecyclerAdapter(charInfoList)

        initializeUi()
    }

    override fun onBackPressed() {
        if (backPressed + Constants.BACK_TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(baseContext, R.string.press_back, Toast.LENGTH_SHORT).show()
        }
        backPressed = System.currentTimeMillis()
    }

    private fun initializeUi() {
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        recycler.layoutManager = linearLayoutManager
        recycler.adapter = recyclerAdapter
        characterViewModel.clear()

        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, charsArray)
        spinner.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            getCharacterInfoList()
        }

        btnSelect.setOnClickListener {
            val dialogueIntent = Intent(this@SelectorActivity, DialogueActivity::class.java)
            dialogueIntent.putExtra("char", spinner.selectedItem.toString())
            startActivity(dialogueIntent)
        }
    }

    private fun getCharacterInfoList() {
        characterViewModel.fetchCharListFromDb(object : DatabaseCallback {
            override fun onResponse(response: IDatabaseResponse) {
                if (response.data != null) {
                    charsArray.addAll(response.data as ArrayList<String>)
                    charsArray.forEach { charName ->
                        getImageFileNameOfChar(charName)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun getImageFileNameOfChar(charName: String) {
        characterViewModel.getImageFileName(charName, object : DatabaseCallback {
            override fun onResponse(response: IDatabaseResponse) {
                if (response.data != null) {
                    getImageFile(charName, response.data as String)
                }
            }
        })
    }

    private fun getImageFile(charName: String, fileName: String) {
        storageViewModel.getImageFile(object : DatabaseCallback {
            override fun onResponse(response: IDatabaseResponse) {
                charInfoList.add(CharacterInfo(response.data as Bitmap?, charName))
                recyclerAdapter.notifyDataSetChanged()
//                if (response.data != null) {
//                    charInfoList.add(CharacterInfo(response.data as File, charName))
//                    recyclerAdapter.notifyDataSetChanged()
//                }
            }
        }, charName, fileName)
    }

}