package com.example.bestuiintheworld.fragments.address

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bestuiintheworld.R
import com.example.bestuiintheworld.commons.BrazilUtils.BrazilianCities
import com.example.bestuiintheworld.commons.StringUtils.normalizeWord
import com.example.bestuiintheworld.components.OnItemClickListener

class CityAdapter (private val listener: OnItemClickListener) :
    RecyclerView.Adapter<CityAdapter.WordViewHolder>() {

    private val fullList = BrazilianCities.brazilianCities.shuffled()
    private var filteredList = fullList.toMutableList()

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

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.wordText.text = filteredList[position]
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            fullList.toMutableList()
        } else {
            val mappedQuery = query.lowercase().groupingBy { it }.eachCount()

            fullList.filter { city ->
                val charGroup = city.lowercase().normalizeWord().groupingBy { it }.eachCount()
                mappedQuery.none { (letter, _) ->
                    charGroup.getOrDefault(letter, 0) > 0
                }
            }.toMutableList()

        }
        notifyDataSetChanged()
    }
}