package com.example.upworkapp

// EventAdapter.kt


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(
    private var events: List<Event>,
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textViewEventTitle)
        val timeTextView: TextView = view.findViewById(R.id.textViewEventTime)
        val locationTextView: TextView = view.findViewById(R.id.textViewEventLocation)
        val deleteButton: ImageButton = view.findViewById(R.id.buttonDeleteEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.titleTextView.text = event.title

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.timeTextView.text = timeFormat.format(event.dateTime)

        if (event.location.isNotEmpty()) {
            holder.locationTextView.visibility = View.VISIBLE
            holder.locationTextView.text = event.location
        } else {
            holder.locationTextView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(position) }
        holder.deleteButton.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount() = events.size

    fun updateData(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}