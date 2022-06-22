package com.smf.events.ui.timeslotsexpandablelist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.ui.timeslotsexpandablelist.model.BookedEventsList

class BookedSlotsRecyclerViewAdapter(private val myEventsList: java.util.ArrayList<BookedEventsList>) :
    RecyclerView.Adapter<BookedSlotsRecyclerViewAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.booked_event_list_recycler_view_item, parent, false)
        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.date.text = myEventsList[position].date
        holder.eventName.text = myEventsList[position].eventName
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return myEventsList.size
    }

    // Holds the views for adding it to text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val date: TextView = itemView.findViewById(R.id.event_date_display)
        val eventName: TextView = itemView.findViewById(R.id.event_name_display)
    }

}