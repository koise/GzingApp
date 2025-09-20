package com.example.gzingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gzingapp.R
import com.example.gzingapp.data.NavigationActivityLog
import java.text.SimpleDateFormat
import java.util.*

class NavigationLogsAdapter(
    private var logs: List<NavigationActivityLog> = emptyList(),
    private val onLogClick: (NavigationActivityLog) -> Unit = {},
    private val onNavigateAgain: (NavigationActivityLog) -> Unit = {}
) : RecyclerView.Adapter<NavigationLogsAdapter.LogViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivStatusIcon: ImageView = itemView.findViewById(R.id.ivStatusIcon)
        val tvDestinationName: TextView = itemView.findViewById(R.id.tvDestinationName)
        val tvDestinationAddress: TextView = itemView.findViewById(R.id.tvDestinationAddress)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTransportMode: TextView = itemView.findViewById(R.id.tvTransportMode)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        val btnViewDetails: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btnViewDetails)
        val btnNavigateAgain: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.btnNavigateAgain)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_navigation_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        
        // Set status icon
        setStatusIcon(holder, log)
        
        // Set destination info
        holder.tvDestinationName.text = log.destinationName ?: "Unknown Destination"
        holder.tvDestinationAddress.text = log.destinationAddress ?: "Unknown Address"
        
        // Set status
        holder.tvStatus.text = when (log.activityType) {
            "navigation_start" -> "Started"
            "navigation_stop" -> "Cancelled"
            "destination_reached" -> "Completed"
            else -> log.activityType.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
        
        // Set transport mode
        holder.tvTransportMode.text = log.transportMode ?: "Unknown"
        
        // Set distance
        holder.tvDistance.text = log.routeDistance?.let { "%.1f km".format(it) } ?: "N/A"
        
        // Set duration
        holder.tvDuration.text = log.navigationDuration?.let { "$it min" } ?: "N/A"
        
        // Set date and time
        log.createdAt?.let { dateString ->
            try {
                // Try different date formats
                val date = when {
                    dateString.contains("T") -> {
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateString)
                    }
                    dateString.contains(" ") -> {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
                    }
                    else -> {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                    }
                }
                date?.let {
                    holder.tvDateTime.text = "${dateFormat.format(it)} at ${timeFormat.format(it)}"
                } ?: run {
                    holder.tvDateTime.text = dateString
                }
            } catch (e: Exception) {
                holder.tvDateTime.text = dateString
            }
        } ?: run {
            holder.tvDateTime.text = "Unknown"
        }
        
        // Set click listeners
        holder.btnViewDetails.setOnClickListener { onLogClick(log) }
        holder.btnNavigateAgain.setOnClickListener { onNavigateAgain(log) }
    }

    override fun getItemCount(): Int = logs.size

    fun updateLogs(newLogs: List<NavigationActivityLog>) {
        android.util.Log.d("NavigationLogsAdapter", "updateLogs called with ${newLogs.size} logs")
        logs = newLogs
        notifyDataSetChanged()
        android.util.Log.d("NavigationLogsAdapter", "notifyDataSetChanged called")
    }

    private fun setStatusIcon(holder: LogViewHolder, log: NavigationActivityLog) {
        when (log.activityType) {
            "navigation_start" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_navigation)
            }
            "navigation_stop" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_close)
            }
            "destination_reached" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_location_pin)
            }
            "navigation_pause" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_pause)
            }
            "navigation_resume" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_play)
            }
            "route_change" -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_route)
            }
            else -> {
                holder.ivStatusIcon.setImageResource(R.drawable.ic_navigation)
            }
        }
        
        holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.primary))
    }
}