package com.neverim.talkinghistory.data.models.adapters

import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.neverim.talkinghistory.R
import com.neverim.talkinghistory.data.models.CharacterInfo
import com.neverim.talkinghistory.ui.DialogueActivity

class CharRecyclerAdapter(private val charInfoList: ArrayList<CharacterInfo>) :
    RecyclerView.Adapter<CharRecyclerAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // Inflate the item Layout
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_row, parent, false
        )

        // Set the view's size, margins, paddings and layout parameters
        return ItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        // Set the data in items
        holder.charNameTextView.text = charInfoList[position].charName

        holder.charImageView.setOnClickListener {
            val context = holder.charImageView.context
            val dialogueIntent = Intent(context, DialogueActivity::class.java)
            dialogueIntent.putExtra("char", charInfoList[position].charName)
            context.startActivity(dialogueIntent)
        }

        if (charInfoList[position].pictureBitmap != null) {
            holder.charImageView.setImageBitmap(charInfoList[position].pictureBitmap)
            holder.charImageView.clipToOutline = true
            holder.charProgressBar.visibility = View.INVISIBLE
            holder.charImageView.visibility = View.VISIBLE
        }
        else {
            holder.charProgressBar.visibility = View.VISIBLE
            holder.charImageView.visibility = View.INVISIBLE
        }

        if (charInfoList[position].charDesc == "" || charInfoList[position].charDesc == null) {
            holder.charDescTextView.visibility = View.INVISIBLE
        }
        else {
            holder.charDescTextView.text = charInfoList[position].charDesc
            holder.charDescTextView.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return charInfoList.size
    }

    fun addImageToChar(charName: String, image: Bitmap) {
        charInfoList.forEachIndexed { index, char ->
            if (char.charName == charName) {
                char.pictureBitmap = image
                notifyItemChanged(index)
            }
        }
    }

    private fun removeAt(position: Int) {
        charInfoList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, charInfoList.size)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Init the item view's
        var charImageView: ImageView = itemView.findViewById(R.id.iv_char_image)
        var charNameTextView: TextView = itemView.findViewById(R.id.tv_char_name)
        var charDescTextView:TextView = itemView.findViewById(R.id.tv_char_desc)
        var charProgressBar: ProgressBar = itemView.findViewById(R.id.pb_recycler_picture)
    }
}