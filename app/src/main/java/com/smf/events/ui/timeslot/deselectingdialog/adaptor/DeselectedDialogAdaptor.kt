package com.smf.events.ui.timeslot.deselectingdialog.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.smf.events.R
import com.smf.events.ui.timeslot.deselectingdialog.model.ListData

// 2803 Deselecting Dialog
class DeselectedDialogAdaptor(listData: ArrayList<ListData>, context: Context) :
    ArrayAdapter<ListData>(context, R.layout.deselected_dialog_listview, listData) {

    private class ViewHolder {
        lateinit var date: TextView
        lateinit var event: TextView
        lateinit var slot: TextView
        lateinit var branch: TextView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder
        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.deselected_dialog_listview, parent, false)
            viewHolder = ViewHolder()
            viewHolder.date = view!!.findViewById<View>(R.id.date_tx) as TextView
            viewHolder.event = view.findViewById<View>(R.id.event_name) as TextView
            viewHolder.slot = view.findViewById<View>(R.id.slots_tx) as TextView
            viewHolder.branch = view.findViewById<View>(R.id.branch_tx) as TextView
        } else {
            viewHolder = view.tag as ViewHolder
        }
        val services = getItem(position)
        viewHolder.date.text = services!!.date
        viewHolder.event.text = services.eventName
        viewHolder.slot.text = services.slots
        view.tag = viewHolder
        return view
    }
}