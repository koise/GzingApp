package com.example.gzingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gzingapp.R
import com.example.gzingapp.data.NavigationRoute
import java.text.SimpleDateFormat
import java.util.*

class NavigationRouteAdapter(
    private val onItemClick: (NavigationRoute) -> Unit,
    private val onNavigateAgain: (NavigationRoute) -> Unit,
    private val onToggleFavorite: (NavigationRoute) -> Unit
) : RecyclerView.Adapter<NavigationRouteAdapter.RouteViewHolder>() {

    private var routes = mutableListOf<NavigationRoute>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun updateRoutes(newRoutes: List<NavigationRoute>) {
        routes.clear()
        routes.addAll(newRoutes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_navigation_route, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    override fun getItemCount(): Int = routes.size

    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRouteName: TextView = itemView.findViewById(R.id.tvRouteName)
        private val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        private val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        private val tvEstimatedFare: TextView = itemView.findViewById(R.id.tvEstimatedFare)
        private val tvTransportMode: TextView = itemView.findViewById(R.id.tvTransportMode)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
        private val ivTransportIcon: ImageView = itemView.findViewById(R.id.ivTransportIcon)

        fun bind(route: NavigationRoute) {
            tvRouteName.text = route.routeName
            tvDestination.text = route.destinationName
            tvDistance.text = String.format("%.2f km", route.routeDistance.toDoubleOrNull() ?: 0.0)
            
            // Display estimated fare with default calculation if null
            val estimatedFare = route.estimatedFare?.toDoubleOrNull() ?: calculateDefaultFare(route.routeDistance.toDoubleOrNull() ?: 0.0)
            tvEstimatedFare.text = String.format("₱%.2f", estimatedFare)
            
            // Provide default transport mode if empty
            val transportMode = route.transportMode.ifBlank { "driving" }
            tvTransportMode.text = transportMode.replaceFirstChar { it.uppercase() }
            
            // Set favorite icon
            if (route.isFavorite == 1) {
                ivFavorite.setImageResource(R.drawable.ic_favorite)
                ivFavorite.setColorFilter(itemView.context.getColor(R.color.error))
            } else {
                ivFavorite.setImageResource(R.drawable.ic_favorite_border)
                ivFavorite.setColorFilter(itemView.context.getColor(R.color.text_secondary))
            }
            
            // Set transport mode icon
            val transportIcon = when (transportMode.lowercase()) {
                "driving", "car" -> R.drawable.ic_directions_car
                "walking", "walk" -> R.drawable.ic_navigation
                "cycling", "bike" -> R.drawable.ic_favorite
                else -> R.drawable.ic_directions_car
            }
            ivTransportIcon.setImageResource(transportIcon)
            
            // Set click listeners
            itemView.setOnClickListener { onItemClick(route) }
            ivFavorite.setOnClickListener { onToggleFavorite(route) }
            
            // Add navigate again button
            itemView.findViewById<View>(R.id.btnNavigateAgain)?.setOnClickListener {
                onNavigateAgain(route)
            }
        }
        
        private fun calculateDefaultFare(distanceKm: Double): Double {
            // 15 pesos base for first 1 kilometer, 2 pesos for exceeding kilometers
            val base = 15.0
            val extra = if (distanceKm > 1.0) (distanceKm - 1.0) * 2.0 else 0.0
            return base + extra
        }
    }
}
