package com.neverim.talkinghistory.views

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.IDatabaseCallback
import com.neverim.talkinghistory.data.CharacterInfo
import com.neverim.talkinghistory.data.DatabaseResponse
import com.neverim.talkinghistory.adapters.CharRecyclerAdapter
import com.neverim.talkinghistory.viewmodels.CharacterViewModel
import com.neverim.talkinghistory.viewmodels.CharacterViewModelFactory
import com.neverim.talkinghistory.viewmodels.StorageViewModel
import com.neverim.talkinghistory.viewmodels.StorageViewModelFactory
import com.neverim.talkinghistory.utilities.Constants
import com.neverim.talkinghistory.utilities.HelperUtils
import com.neverim.talkinghistory.utilities.InjectorUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception


class SelectorActivity : AppCompatActivity() {

    private val LOG_TAG = this.javaClass.simpleName

    // View elements and adapters
    private lateinit var recycler: RecyclerView
    private lateinit var recyclerAdapter: CharRecyclerAdapter

    // ViewModels
    private lateinit var characterViewModel: CharacterViewModel
    private lateinit var storageViewModel: StorageViewModel

    // ViewModel factories
    private val characterFactory: CharacterViewModelFactory = InjectorUtils.provideCharacterViewModelFactory()
    private val storageFactory: StorageViewModelFactory = InjectorUtils.provideStorageViewModelFactory()

    // Variables
    private lateinit var brokenImage: Bitmap
    private lateinit var loadingBar: ProgressBar
    private var charsArray: ArrayList<String> = ArrayList()
    private var charInfoList: ArrayList<CharacterInfo> = ArrayList()
    private var backPressed: Long = 0
    private var alert: AlertDialog? = null
    private var recyclerVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_char)

        checkConnection()

        recycler = findViewById(R.id.rv_select_char)
        loadingBar = findViewById(R.id.pb_recycler)

        characterViewModel = ViewModelProvider(this, characterFactory).get(CharacterViewModel::class.java)
        storageViewModel = ViewModelProvider(this, storageFactory).get(StorageViewModel::class.java)
        recyclerAdapter = CharRecyclerAdapter(charInfoList)

        Log.i(LOG_TAG, "asking for permissions")

        HelperUtils.checkPermissions(this)

        initializeUi()
    }

    override fun onBackPressed() {
        if (backPressed + Constants.BACK_TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        }
        else {
            Toast.makeText(baseContext, R.string.press_back, Toast.LENGTH_SHORT).show()
        }
        backPressed = System.currentTimeMillis()
    }

    private fun initializeUi() {
        val linearLayoutManager = LinearLayoutManager(this)
        recycler.layoutManager = linearLayoutManager
        recycler.adapter = recyclerAdapter
        characterViewModel.clear()

        CoroutineScope(Dispatchers.IO).launch {
            ResourcesCompat.getDrawable(this@SelectorActivity.resources, R.drawable.ic_broken_image, theme)?.let {
                brokenImage = HelperUtils.drawableToBitmap(it)!!
            }
            getCharacterInfoList()
        }
    }

    private fun getCharacterInfoList() {
        characterViewModel.fetchCharListFromDb(object : IDatabaseCallback {
            override fun onResponse(response: DatabaseResponse) {
                if (response.data != null) {
                    charsArray.addAll(response.data as ArrayList<String>)
                    charsArray.forEach { charName ->
                        getDescription(charName)
                    }
                }
            }
        })
    }

    private fun getDescription(charName: String) {
        characterViewModel.getDescription(object : IDatabaseCallback {
            override fun onResponse(response: DatabaseResponse) {
                val desc = response.data as String?
                charInfoList.add(CharacterInfo(null, charName, desc))
                recyclerAdapter.notifyDataSetChanged()
                showRecycler()
                characterViewModel.getImageFileName(object : IDatabaseCallback {
                    override fun onResponse(response: DatabaseResponse) {
                        val imageFileName = response.data.toString()
                        addImageToChar(charName, imageFileName)
                    }
                }, charName)
            }
        }, charName)
    }

    private fun addImageToChar(charName: String, imageFileName: String?) {
        storageViewModel.getImageFile(object : IDatabaseCallback {
            override fun onResponse(response: DatabaseResponse) {
                if (response.exception is Exception) {
                    recyclerAdapter.addImageToChar(charName, brokenImage)
                }
                else {
                    recyclerAdapter.addImageToChar(charName, response.data as Bitmap)
                }
            }
        }, charName, imageFileName)
    }

    private fun checkConnection() {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (alert != null) {
                    alert!!.dismiss()
                    alert = null
                    connectivityManager.unregisterNetworkCallback(this)
                }
            }
        })
        if (connectivityManager.activeNetwork == null && alert == null) {
            showAlert()
        }
    }

    private fun showRecycler() {
        if (!recyclerVisible) {
            loadingBar.visibility = View.INVISIBLE
            recycler.visibility = View.VISIBLE
            recyclerVisible = true
        }
    }

    private fun showAlert() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Make sure that Wi-Fi or mobile data is turned on, then try again")
            .setCancelable(false)
        alert = dialogBuilder.create()
        alert!!.setTitle("No Internet Connection")
        alert!!.setIcon(R.drawable.ic_bad_connection)
        alert!!.show()
    }

}