package com.example.gzingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gzingapp.R
import com.example.gzingapp.data.NavigationHistory
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class NavigationHistoryAdapter(
    private val onItemClick: (NavigationHistory) -> Unit,
    private val onNavigateAgain: (NavigationHistory) -> Unit,
    private val onToggleFavorite: (NavigationHistory) -> Unit
) : RecyclerView.Adapter<NavigationHistoryAdapter.HistoryViewHolder>() {
    
    private var historyItems = mutableListOf<NavigationHistory>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    
    fun updateHistory(newHistoryItems: List<NavigationHistory>) {
        historyItems.clear()
        historyItems.addAll(newHistoryItems)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_navigation_history, parent, false)
        return HistoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyItems[position])
    }
    
    override fun getItemCount(): Int = historyItems.size
    
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardHistory)
        private val ivDestinationIcon: ImageView = itemView.findViewById(R.id.ivDestinationIcon)
        private val tvDestinationName: TextView = itemView.findViewById(R.id.tvDestinationName)
        // tvStopInfo removed as it's no longer in the layout
        private val tvDestinationCoordinates: TextView = itemView.findViewById(R.id.tvDestinationCoordinates)
        private val tvEstimatedFare: TextView = itemView.findViewById(R.id.tvEstimatedFare)
        // tvStartTime, tvEndTime, tvCompletionTime removed as they don't exist in the layout
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
        private val btnViewDetails: MaterialButton = itemView.findViewById(R.id.btnViewDetails)
        private val btnNavigateAgain: MaterialButton = itemView.findViewById(R.id.btnNavigateAgain)
        private val btnFavorite: MaterialButton = itemView.findViewById(R.id.btnFavorite)
        
        fun bind(history: NavigationHistory) {
            // Set destination name (coordinates format)
            tvDestinationName.text = history.destinationName
            
            // Set destination coordinates (formatted as coordinates)
            tvDestinationCoordinates.text = String.format("%.6f, %.6f", history.endLatitude, history.endLongitude)
            
            // Set estimated fare
            tvEstimatedFare.text = String.format("₱%.2f", history.estimatedFare ?: 0.0)
            
            // Time information removed as TextViews don't exist in the layout
            
            // Set destination icon
            ivDestinationIcon.setImageResource(R.drawable.ic_location_pin)
            
            // Set favorite icon
            val isFavorite = history.isFavorite
            if (isFavorite) {
                ivFavorite.setImageResource(R.drawable.ic_favorite)
                ivFavorite.setColorFilter(itemView.context.getColor(R.color.error))
                ivFavorite.visibility = View.VISIBLE
            } else {
                ivFavorite.visibility = View.GONE
            }
            
            // Set favorite button
            btnFavorite.text = if (isFavorite) "❤️" else "🤍"
            
            // Set click listeners
            cardView.setOnClickListener { onItemClick(history) }
            btnViewDetails.setOnClickListener { onItemClick(history) }
            btnNavigateAgain.setOnClickListener { onNavigateAgain(history) }
            btnFavorite.setOnClickListener { onToggleFavorite(history) }
        }
        
        private fun formatTime(timeString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(timeString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                timeString
            }
        }
    }
}
