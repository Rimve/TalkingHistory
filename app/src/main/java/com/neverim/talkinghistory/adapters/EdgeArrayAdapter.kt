package com.neverim.talkinghistory.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.models.Edge
import com.neverim.talkinghistory.models.NodeEntry

class EdgeArrayAdapter(private val context: Context, private val arrayList: ArrayList<Edge<NodeEntry>>) : BaseAdapter() {
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
        var cView = convertView
        cView = LayoutInflater.from(context).inflate(R.layout.edge_row, parent, false)
        question = cView.findViewById(R.id.edge_row_textView)
        question.text = arrayList[position].destination.data.entry
        return cView
    }

    fun clear() {
        arrayList.clear()
    }

    fun add(arrayList: ArrayList<Edge<NodeEntry>>) {
        arrayList.addAll(arrayList)
    }
}