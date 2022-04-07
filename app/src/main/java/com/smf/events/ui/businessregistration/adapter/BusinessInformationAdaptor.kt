package com.smf.events.ui.businessregistration.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R

class BusinessInformationAdaptor:
    RecyclerView.Adapter<BusinessInformationAdaptor.BusinessInfoViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BusinessInformationAdaptor.BusinessInfoViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.business_information_cardview, parent, false)
        return BusinessInfoViewHolder(itemView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: BusinessInformationAdaptor.BusinessInfoViewHolder,
        position: Int,
    ) {
        with(holder){

            val isExpandable: Boolean = expand
            expandableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE

            businessInfoTitleLayout.setOnClickListener {
                expand = !expand
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class BusinessInfoViewHolder(val view: View):RecyclerView.ViewHolder(view) {
        var expand : Boolean = false
        var businessInfoTitleLayout: ConstraintLayout = view.findViewById(R.id.business_info_layout)
        var expandableLayout: ConstraintLayout = view.findViewById(R.id.expandable_layout)
    }
}