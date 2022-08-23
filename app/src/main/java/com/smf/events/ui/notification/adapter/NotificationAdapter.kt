package com.smf.events.ui.notification.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.ui.notification.model.NotificationDetails

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.MyNotificationViewHolder>() {

    private var notificationList = ArrayList<NotificationDetails>()
    private var onClickListener: OnNotificationClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.MyNotificationViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_card_view, parent, false)
        return MyNotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: NotificationAdapter.MyNotificationViewHolder,
        position: Int
    ) {
        holder.onBind(notificationList[position])

        holder.itemView.setOnClickListener {
            onClickListener?.onNotificationClicked(notificationList[position], position)
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    inner class MyNotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var date: TextView = view.findViewById(R.id.date_time_text)
        var title: TextView = view.findViewById(R.id.bid_status_title)
        var description: TextView = view.findViewById(R.id.bid_status_des)
        var image: ImageView = view.findViewById(R.id.imageView2)

        // Method For Fixing xml views and Values
        fun onBind(notification: NotificationDetails) {
            date.text = notification.notificationDate
            title.text = notification.notificationTitle
            description.text = notification.description
//            TODO set imageview

        }
    }

    //Method For Refreshing notifications
    @SuppressLint("NotifyDataSetChanged")
    fun refreshItems(notification: List<NotificationDetails>) {
        notificationList.clear()
        notificationList.addAll(notification)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(position: Int){
        notificationList.removeAt(position)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(position: Int, notificationDetail: NotificationDetails){
        notificationList.add(notificationDetail)
        notifyDataSetChanged()
    }

    // Initializing Listener Interface
    fun setOnClickListener(listener: OnNotificationClickListener) {
        onClickListener = listener
    }

    // Interface For Invoice Click Listener
    interface OnNotificationClickListener {
        fun onNotificationClicked(notification: NotificationDetails, position: Int)
    }

}