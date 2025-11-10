package com.example.volunteerapp.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.volunteerapp.databinding.ItemEventAdminBinding
import com.example.volunteerapp.model.Event

class EventAdapter(
    private val events: List<Event>,
    private val onEditClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemEventAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.txtTitle.text = event.title
            binding.txtDate.text = event.date
            binding.txtLocation.text = event.location
            binding.txtVolunteers.text = "Volunteers: ${event.totalVolunteers}"

            binding.btnEdit.setOnClickListener { onEditClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = events.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }
}
