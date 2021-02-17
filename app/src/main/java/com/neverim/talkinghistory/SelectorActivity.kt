package com.neverim.talkinghistory

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SelectorActivity : AppCompatActivity() {
    private lateinit var spinner: Spinner
    private lateinit var selectBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_char)

        spinner = findViewById(R.id.spinner)
        selectBtn = findViewById(R.id.btn_select_char)

        val characters = resources.getStringArray(R.array.Characters)

        if (spinner != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, characters)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                    Toast.makeText(this@SelectorActivity,
                        getString(R.string.selected_item) + " " +
                                "" + characters[position], Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }

        selectBtn.setOnClickListener {
            val dialogueIntent = Intent(this@SelectorActivity, DialogueActivity::class.java)
            dialogueIntent.putExtra("char", spinner.selectedItem.toString())
            startActivity(dialogueIntent)
        }
    }
}