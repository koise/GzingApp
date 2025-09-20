package com.example.gzingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gzingapp.R
import com.example.gzingapp.data.SosContact
import com.example.gzingapp.ui.components.InitialsAvatar

class SosContactsAdapter(
    private var contacts: List<SosContact> = emptyList(),
    private var onEditClick: ((SosContact) -> Unit)? = null,
    private var onDeleteClick: ((SosContact) -> Unit)? = null
) : RecyclerView.Adapter<SosContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarContact: InitialsAvatar = itemView.findViewById(R.id.avatarContact)
        val tvContactName: TextView = itemView.findViewById(R.id.tvContactName)
        val tvContactPhone: TextView = itemView.findViewById(R.id.tvContactPhone)
        val tvContactRelationship: TextView = itemView.findViewById(R.id.tvContactRelationship)
        val btnEditContact: ImageButton = itemView.findViewById(R.id.btnEditContact)
        val btnDeleteContact: ImageButton = itemView.findViewById(R.id.btnDeleteContact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sos_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        
        // Set contact information
        holder.tvContactName.text = contact.name
        holder.tvContactPhone.text = contact.phoneNumber
        holder.tvContactRelationship.text = contact.relationship
        
        // Set initials avatar
        holder.avatarContact.setInitialsFromName(contact.name)
        holder.avatarContact.setTextSize(16f)
        
        // Set up edit button click listener
        holder.btnEditContact.setOnClickListener {
            onEditClick?.invoke(contact)
        }
        
        // Set up delete button click listener
        holder.btnDeleteContact.setOnClickListener {
            onDeleteClick?.invoke(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateContacts(newContacts: List<SosContact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
    
    fun setOnEditClickListener(listener: (SosContact) -> Unit) {
        onEditClick = listener
    }
    
    fun setOnDeleteClickListener(listener: (SosContact) -> Unit) {
        onDeleteClick = listener
    }
}
