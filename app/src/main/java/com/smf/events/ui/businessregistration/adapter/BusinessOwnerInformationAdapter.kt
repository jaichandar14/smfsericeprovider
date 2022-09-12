package com.smf.events.ui.businessregistration.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R

class BusinessOwnerInformationAdapter :
    RecyclerView.Adapter<BusinessOwnerInformationAdapter.BusinessRegistrationViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BusinessOwnerInformationAdapter.BusinessRegistrationViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.business_owner_info_card_view, parent, false)
        return BusinessRegistrationViewHolder(itemView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: BusinessOwnerInformationAdapter.BusinessRegistrationViewHolder,
        position: Int,
    ) {

        with(holder) {
            val isExpandable: Boolean = expand
            expandableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE

            businessOwnerInfoLayout.setOnClickListener {
                expand = !expand
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class BusinessRegistrationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var expand: Boolean = false
        var businessOwnerInfoLayout: ConstraintLayout =
            view.findViewById(R.id.business_owner_info_layout)
        var expandableLayout: ConstraintLayout =
            view.findViewById(R.id.business_owner_info_expandable_layout)
    }
}