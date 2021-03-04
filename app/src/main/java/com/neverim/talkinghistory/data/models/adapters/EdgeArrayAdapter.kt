package com.neverim.talkinghistory.data.models.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.models.Edge

class EdgeArrayAdapter(private val context: Context, private val arrayList: ArrayList<Edge>) : BaseAdapter() {
    private lateinit var question: TextView

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return arrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val cView = LayoutInflater.from(context).inflate(R.layout.edge_row, parent, false)
        question = cView.findViewById(R.id.edge_row_textView)
        question.text = arrayList[position].destination.data
        return cView
    }
}