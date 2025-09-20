package com.example.bestuiintheworld.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bestuiintheworld.R
import com.example.bestuiintheworld.components.OnItemClickListener

class CommonListAdapter (
    private val items: List<String>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<CommonListAdapter.WordViewHolder>() {

    inner class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                val selectedCity = items[bindingAdapterPosition]
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

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.wordText.text = items[position]
    }
}