package com.smf.events.ui.notification.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.helper.AppConstants
import com.smf.events.ui.notification.callbacks.CardViewClear
import com.smf.events.ui.notification.model.NotificationDetails

class NotificationAdapter(private val tag: String) :
    RecyclerView.Adapter<NotificationAdapter.MyNotificationViewHolder>() {

    private var notificationList = ArrayList<NotificationDetails>()
    private var onClickListener: OnNotificationClickListener? = null
    private var clearButtonListener: CardViewClear? = null

    // Initializing Clear Listener Interface
    fun setOnClickClearListener(listener: CardViewClear) {
        clearButtonListener = listener
    }

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

        holder.clearBtn.setOnClickListener {
            if (tag == AppConstants.ACTIVE) {
                clearButtonListener?.onClearButtonClicked(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    inner class MyNotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var date: TextView = view.findViewById(R.id.date_time_text)
        var clearBtn: ImageView = view.findViewById(R.id.clear_btn)
        var title: TextView = view.findViewById(R.id.bid_status_title)
        var description: TextView = view.findViewById(R.id.bid_status_des)
        var image: ImageView = view.findViewById(R.id.imageView2)

        // Method For Fixing xml views and Values
        fun onBind(notification: NotificationDetails) {
            if (tag == AppConstants.ACTIVE) {
                clearBtn.visibility = View.VISIBLE
            } else {
                clearBtn.visibility = View.GONE
            }
            date.text = notification.notificationDate
            title.text = notification.notificationTitle
            description.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(notification.notificationContent, Html.FROM_HTML_MODE_COMPACT)
            } else {
                HtmlCompat.fromHtml(
                    notification.notificationContent,
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
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

    // Initializing Listener Interface
    fun setOnClickListener(listener: OnNotificationClickListener) {
        onClickListener = listener
    }

    // Interface For Invoice Click Listener
    interface OnNotificationClickListener {
        fun onNotificationClicked(notification: NotificationDetails, position: Int)
    }

}