package com.example.bestuiintheworld.fragments.extrainfo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bestuiintheworld.R
import com.example.bestuiintheworld.commons.Utils
import com.example.bestuiintheworld.components.OnItemClickListener

class StreamerAdapter (private val listener: OnItemClickListener) :
    RecyclerView.Adapter<StreamerAdapter.WordViewHolder>() {

    private val fullList = Utils.bestStreamersOfBrazil

    inner class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                val selectedCity = fullList[adapterPosition]
                listener.onItemClick(selectedCity)
            }
        }

        val wordText: TextView = view.findViewById(R.id.textViewWord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_view, parent, false)
        return WordViewHolder(view)
    }

    override fun getItemCount(): Int = fullList.size

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.wordText.text = fullList[position]
    }
}