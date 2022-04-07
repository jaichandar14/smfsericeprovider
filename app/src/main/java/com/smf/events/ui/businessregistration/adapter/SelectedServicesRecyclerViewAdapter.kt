package com.smf.events.ui.businessregistration.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.ui.businessregistration.model.SelectedServices

class SelectedServicesRecyclerViewAdapter(private val context: Context, private val items : ArrayList<SelectedServices>): RecyclerView.Adapter<SelectedServicesRecyclerViewAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectedServicesRecyclerViewAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.selected_service_card_view, parent, false))
    }

    override fun onBindViewHolder(holder: SelectedServicesRecyclerViewAdapter.ViewHolder, position: Int) {

        holder.serviceName.text = items[position].serviceTitle

        holder.closeBtn.setOnClickListener {
            Toast.makeText(context, "close clicked ${items[position].serviceTitle}", Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        var closeBtn = itemView.findViewById<ImageView>(R.id.cancel_btn)
        var imageView = itemView.findViewById<ImageView>(R.id.service_image)
        var serviceName = itemView.findViewById<TextView>(R.id.service_name_text)
    }
}