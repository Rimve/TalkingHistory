package com.neverim.talkinghistory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var textView: TextView = findViewById(R.id.textView)
        var answerTextView: EditText = findViewById(R.id.editText)
    }
}
