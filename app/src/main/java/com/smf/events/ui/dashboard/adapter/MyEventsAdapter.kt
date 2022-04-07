package com.smf.events.ui.dashboard.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.ui.dashboard.model.MyEvents

class MyEventsAdapter: RecyclerView.Adapter<MyEventsAdapter.MyEventViewHolder>() {

    private var myEventsList = ArrayList<MyEvents>()
    private var onClickListener: OnServiceClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyEventViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.events_card_view, parent, false)
        return MyEventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyEventViewHolder, position: Int) {
         holder.onBind(myEventsList[position])

    }

    override fun getItemCount(): Int {
        return myEventsList.size
    }
    inner class MyEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var titleText = view.findViewById<TextView>(R.id.title_text)
        var numberText = view.findViewById<TextView>(R.id.number_text)

        // Method For Fixing xml views and Values
        fun onBind(myEvents: MyEvents) {
            titleText.text = myEvents.titleText
            numberText.text =  myEvents.numberText

        }
    }

    //Method For Refreshing Invoices
    @SuppressLint("NotifyDataSetChanged")
    fun refreshItems(invoice: List<MyEvents>) {
        myEventsList.clear()
        myEventsList.addAll(invoice)
        notifyDataSetChanged()
    }

    // Initializing Listener Interface
    fun setOnClickListener(listener: OnServiceClickListener) {
        onClickListener = listener
    }

    // Interface For Invoice Click Listener
    interface OnServiceClickListener {

    }

}