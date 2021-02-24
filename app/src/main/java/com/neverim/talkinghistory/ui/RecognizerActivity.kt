package com.neverim.talkinghistory.ui

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.karumi.dexter.Dexter
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.models.PermissionsListener
import com.neverim.talkinghistory.ui.viewmodels.RecognizerViewModel
import com.neverim.talkinghistory.utilities.InjectorUtils

class RecognizerActivity : AppCompatActivity() {

    private lateinit var btnRecord: Button
    private lateinit var tvSpeech: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognizer)

        btnRecord = findViewById(R.id.btn_recognizer_record)
        tvSpeech = findViewById(R.id.tv_recognizer_speech)

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(PermissionsListener(this))
            .check()

        initializeUi()
    }

    private fun initializeUi() {
        val factory = InjectorUtils.provideRecognizerViewModelFactory(this)
        val viewModel = ViewModelProvider(this, factory).get(RecognizerViewModel::class.java)

        viewModel.audioSetup()

        btnRecord.setOnClickListener {
            if (!viewModel.isRecording()) {
                viewModel.recordAudio()
            } else {
                viewModel.stopAudio()
                tvSpeech.text = viewModel.sampleRecognize()
            }
        }
    }
}