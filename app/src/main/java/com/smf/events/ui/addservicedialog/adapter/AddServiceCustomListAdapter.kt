package com.smf.events.ui.addservicedialog.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.smf.events.R
import com.smf.events.ui.addservicedialog.model.Services

class AddServiceCustomListAdapter(items: ArrayList<Services>, context: Context) :
    ArrayAdapter<Services>(context, R.layout.add_service_list_view, items) {

    private class ViewHolder {
        lateinit var serviceName: TextView
        lateinit var textView: TextView
        lateinit var checkBox: CheckBox
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view = convertView

        val viewHolder: ViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.add_service_list_view, parent, false)

            viewHolder = ViewHolder()
            viewHolder.serviceName = view!!.findViewById<View>(R.id.service_name) as TextView
            viewHolder.textView = view.findViewById<View>(R.id.textView3) as TextView
            viewHolder.checkBox = view.findViewById<View>(R.id.check_box) as CheckBox

        } else {
            viewHolder = view.tag as ViewHolder
        }

        val services = getItem(position)
        viewHolder.serviceName.text = services!!.serviceName
        viewHolder.textView.text = services.title
        viewHolder.checkBox.isChecked = services.checked

        // ClickListener for CheckBox
        viewHolder.checkBox.setOnClickListener {
            val service = getItem(position)
            service!!.checked = !service.checked
        }

        view.tag = viewHolder

        return view
    }

}