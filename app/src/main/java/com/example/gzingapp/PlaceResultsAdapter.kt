package com.example.gzingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaceResultsAdapter(
    private val onPlaceSelected: (SearchResult) -> Unit
) : RecyclerView.Adapter<PlaceResultsAdapter.PlaceViewHolder>() {

    private var results: List<SearchResult> = emptyList()

    fun updateResults(newResults: List<SearchResult>) {
        results = newResults
        notifyDataSetChanged()
    }

    fun clearResults() {
        results = emptyList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_result, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPlaceIcon: ImageView = itemView.findViewById(R.id.ivPlaceIcon)
        private val tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        private val tvPlaceAddress: TextView = itemView.findViewById(R.id.tvPlaceAddress)
        private val tvPlaceCategory: TextView = itemView.findViewById(R.id.tvPlaceCategory)

        fun bind(result: SearchResult) {
            tvPlaceName.text = result.name
            tvPlaceAddress.text = result.address
            tvPlaceCategory.text = result.category

            // Set appropriate icon based on category
            val iconRes = when (result.category) {
                "Restaurant" -> R.drawable.ic_restaurant
                "Hotel" -> R.drawable.ic_hotel
                "Shopping" -> R.drawable.ic_shopping
                "Hospital" -> R.drawable.ic_hospital
                "Education" -> R.drawable.ic_school
                "Religious" -> R.drawable.ic_church
                "Bank" -> R.drawable.ic_bank
                "Gas Station" -> R.drawable.ic_gas_station
                "Pharmacy" -> R.drawable.ic_pharmacy
                "Park" -> R.drawable.ic_park
                else -> R.drawable.ic_place
            }
            ivPlaceIcon.setImageResource(iconRes)

            itemView.setOnClickListener {
                onPlaceSelected(result)
            }
        }
    }
}
