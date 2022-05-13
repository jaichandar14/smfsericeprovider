package com.smf.events.ui.timeslotsexpandablelist.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.smf.events.R
import com.smf.events.ui.timeslotsexpandablelist.model.ListData

class CustomExpandableListAdapter internal constructor(
    private val context: Context,
    private val langs: ArrayList<String>,
    private val topics: HashMap<String, List<ListData>>
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.topics[this.langs[listPosition]]!![expandedListPosition]
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.topics[this.langs[listPosition]]!!.size
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int,
        expandedListPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val expandedListData = getChild(listPosition, expandedListPosition) as ListData
        val layoutInflater =
            this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        convertView = if (expandedListPosition == 0) {
            layoutInflater.inflate(R.layout.time_slot_list, null)
        } else {
            layoutInflater.inflate(R.layout.time_slot_bottom, null)
        }
        var image12To3am = convertView?.findViewById<ImageView>(R.id.image_view_12_3am)
        val timeSlot12To3am = convertView?.findViewById<TextView>(R.id.time_slot_12_3am)
        val address12To3am = convertView?.findViewById<TextView>(R.id.address_12_3am)
        val view12To3am = convertView?.findViewById<View>(R.id.view_12_3am)
        address12To3am?.text = null

        image12To3am?.setOnClickListener {
            Log.d("TAG", "getChildView: clicked $expandedListPosition")
            timeSlotIconOnClickListener?.onClick(expandedListPosition)
        }

        // Get Booked Event Lists Line By Line
        val addressText = StringBuffer()
        for (i in expandedListData.status.indices) {
            addressText.append(expandedListData.status[i].eventName + " " + "Event")
            addressText.append("\n\r")
            addressText.append(expandedListData.status[i].branchName)
            if (i != (expandedListData.status.size - 1)) {
                addressText.append("\n\r")
            }
        }
        address12To3am?.text = addressText
        timeSlot12To3am?.text = expandedListData.timeSlot

        // Loop For Last ChildView View Invisible
        if (isLastChild) {
            view12To3am?.visibility = View.GONE
        } else {
            view12To3am?.visibility = View.VISIBLE
        }

        return convertView!!
    }

    override fun getGroup(listPosition: Int): Any {
        return this.langs[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.langs.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition) as String
        val layoutInflater =
            this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        convertView = if (isExpanded) {
            layoutInflater.inflate(R.layout.time_slot_title, null)
        } else {
            layoutInflater.inflate(R.layout.time_slot_title_collapse, null)
        }
        val titleDateTextView = convertView?.findViewById<TextView>(R.id.title_date_textView)
        val titleDayTextView = convertView?.findViewById<TextView>(R.id.title_day_textView)
        if (isExpanded) {
            convertView?.findViewById<ImageView>(R.id.plus_icon)?.setImageResource(R.drawable.minus)
        } else {
            convertView?.findViewById<ImageView>(R.id.plus_icon)?.setImageResource(R.drawable.plus)
        }
        titleDateTextView?.text = listTitle

        return convertView!!
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return false
    }

    private var timeSlotIconOnClickListener: TimeSlotIconClickListener? = null

    // Initializing TimeSlotIconClickListener Interface
    fun setOnClickListener(listener: TimeSlotIconClickListener) {
        timeSlotIconOnClickListener = listener
    }

    // Interface For TimeSlot Icon Click
    interface TimeSlotIconClickListener {
        fun onClick(expandedListPosition: Int)
    }

}