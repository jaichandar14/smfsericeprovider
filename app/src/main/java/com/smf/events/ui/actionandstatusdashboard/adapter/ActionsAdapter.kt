package com.smf.events.ui.actionandstatusdashboard.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.ui.dashboard.model.MyEvents

class ActionsAdapter(val context: Context) :
    RecyclerView.Adapter<ActionsAdapter.ActionsViewHolder>() {

    private var myEventsList = ArrayList<MyEvents>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ActionsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.actions_status_dashboard_cardview, parent, false)
        return ActionsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ActionsViewHolder, position: Int) {
        holder.onBind(myEventsList[position])
    }

    override fun getItemCount(): Int {
        return myEventsList.size
    }

    //Method For Refreshing Invoices
    @SuppressLint("NotifyDataSetChanged")
    fun refreshItems(invoice: List<MyEvents>) {
        myEventsList.clear()
        myEventsList.addAll(invoice)
        notifyDataSetChanged()
    }

    inner class ActionsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var actionsNum: TextView = view.findViewById(R.id.action_numbers)
        var actionTitle: TextView = view.findViewById(R.id.actions_list)
        var actionCardLayout: CardView = view.findViewById(R.id.action_card_view_layout)

        fun onBind(myEvents: MyEvents) {
            actionsNum.text = myEvents.numberText
            actionTitle.text = myEvents.titleText
            actionCardLayout.setOnClickListener {
                onClickListener?.actionCardClick(myEvents)
            }

        }

    }

    private var onClickListener: OnActionCardClickListener? = null

    // Initializing Listener Interface
    fun setOnClickListener(listener: OnActionCardClickListener) {
        onClickListener = listener
    }

    // Interface For Invoice Click Listener
    interface OnActionCardClickListener {
        fun actionCardClick(myEvents: MyEvents)
    }
}