package com.example.gzingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gzingapp.R
import com.example.gzingapp.data.SosContact

class SosEmergencyAdapter(
    private var contacts: List<SosContact> = emptyList()
) : RecyclerView.Adapter<SosEmergencyAdapter.SosEmergencyViewHolder>() {

    class SosEmergencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContactName: TextView = itemView.findViewById(R.id.tvContactName)
        val tvContactPhone: TextView = itemView.findViewById(R.id.tvContactPhone)
        val tvContactRelationship: TextView = itemView.findViewById(R.id.tvContactRelationship)
        val tvContactInitial: TextView = itemView.findViewById(R.id.tvContactInitial)
        val layoutPrimaryBadge: View = itemView.findViewById(R.id.layoutPrimaryBadge)
        val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SosEmergencyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sos_contact_emergency, parent, false)
        return SosEmergencyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SosEmergencyViewHolder, position: Int) {
        val contact = contacts[position]
        
        // Set contact information
        holder.tvContactName.text = contact.name
        holder.tvContactPhone.text = contact.phoneNumber
        holder.tvContactRelationship.text = contact.relationship
        
        // Set initial letter for avatar
        holder.tvContactInitial.text = contact.name.take(1).uppercase()
        
        // Show/hide primary badge
        holder.layoutPrimaryBadge.visibility = if (contact.isPrimary) View.VISIBLE else View.GONE
        
        // Set status (always ready for emergency)
        holder.statusIndicator.setBackgroundColor(
            holder.itemView.context.getColor(android.R.color.holo_green_light)
        )
        holder.tvStatus.text = "Ready"
        holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
    }

    override fun getItemCount(): Int = contacts.size

    fun updateContacts(newContacts: List<SosContact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    fun getContacts(): List<SosContact> = contacts
}


