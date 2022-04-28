package com.smf.events.ui.timeslotsexpandablelist.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.smf.events.R
import com.smf.events.ui.timeslotsexpandablelist.model.ListData
import com.smf.events.ui.timeslotsexpandablelist.model.TitleData

class CustomExpandableListAdapter internal constructor(
    private val context: Context,
    private val titleList: List<TitleData>,
    private val dataList: HashMap<TitleData, List<List<ListData>>>
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.dataList[this.titleList[listPosition]]!![expandedListPosition]
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataList[this.titleList[listPosition]]!!.size
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
        val expandedListData = getChild(listPosition, expandedListPosition) as List<ListData>
        if (convertView == null) {
            val layoutInflater =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.time_slot_list, null)
        }

        val image12To3am = convertView?.findViewById<ImageView>(R.id.image_view_12_3am)
        val image3To6am = convertView?.findViewById<ImageView>(R.id.image_view_3_6am)
        val image6To9am = convertView?.findViewById<ImageView>(R.id.image_view_6_9am)
        val image9To12pm = convertView?.findViewById<ImageView>(R.id.image_view_9_12pm)
        val image12To3pm = convertView?.findViewById<ImageView>(R.id.image_view_12_3pm)
        val image3To6pm = convertView?.findViewById<ImageView>(R.id.image_view_3_6pm)
        val image6To9pm = convertView?.findViewById<ImageView>(R.id.image_view_6_9pm)
        val image9To12am = convertView?.findViewById<ImageView>(R.id.image_view_9_12am)

        val timeSlot12To3am = convertView?.findViewById<TextView>(R.id.time_slot_12_3am)
        val timeSlot3To6am = convertView?.findViewById<TextView>(R.id.time_slot_3_6am)
        val timeSlot6To9am = convertView?.findViewById<TextView>(R.id.time_slot_6_9am)
        val timeSlot9To12pm = convertView?.findViewById<TextView>(R.id.time_slot_9_12pm)
        val timeSlot12To3pm = convertView?.findViewById<TextView>(R.id.time_slot_12_3pm)
        val timeSlot3To6pm = convertView?.findViewById<TextView>(R.id.time_slot_3_6pm)
        val timeSlot6To9pm = convertView?.findViewById<TextView>(R.id.time_slot_6_9pm)
        val timeSlot9To12am = convertView?.findViewById<TextView>(R.id.time_slot_9_12am)

        val address12To3am = convertView?.findViewById<TextView>(R.id.address_12_3am)
        val address3To6am = convertView?.findViewById<TextView>(R.id.address_3_6am)
        val address6To9am = convertView?.findViewById<TextView>(R.id.address_6_9am)
        val address9To12pm = convertView?.findViewById<TextView>(R.id.address_9_12pm)
        val address12To3pm = convertView?.findViewById<TextView>(R.id.address_12_3pm)
        val address3To6pm = convertView?.findViewById<TextView>(R.id.address_3_6pm)
        val address6To9pm = convertView?.findViewById<TextView>(R.id.address_6_9pm)
        val address9To12am = convertView?.findViewById<TextView>(R.id.address_9_12am)

        image12To3am?.setOnClickListener {
            Log.d("TAG", "getChildView: clicked")
            timeSlotIconOnClickListener?.onClick(listPosition)
        }

        for (i in expandedListData.indices) {
            when (expandedListData[i].timeSlot) {
                "12am-3am" -> {
                    address12To3am?.text = expandedListData[i].status
                }
                "3am-6am" -> {
                    address3To6am?.text = expandedListData[i].status
                }
                "6am-9am" -> {
                    address6To9am?.text = expandedListData[i].status
                }
                "9am-12pm" -> {
                    address9To12pm?.text = expandedListData[i].status
                }
                "12pm-3pm" -> {
                    address12To3pm?.text = expandedListData[i].status
                }
                "3pm-6pm" -> {
                    address3To6pm?.text = expandedListData[i].status
                }
                "6pm-9pm" -> {
                    address6To9pm?.text = expandedListData[i].status
                }
                "9pm-12am" -> {
                    address9To12am?.text = expandedListData[i].status
                }
                else -> {}
            }
        }
        return convertView!!
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleList[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleList.size
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
        val listTitle = getGroup(listPosition) as TitleData
        if (convertView == null) {
            val layoutInflater =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.time_slot_title, null)
        }
        val titleDateTextView = convertView?.findViewById<TextView>(R.id.title_date_textView)
        val titleDayTextView = convertView?.findViewById<TextView>(R.id.title_day_textView)
        if (isExpanded) {
            convertView?.findViewById<ImageView>(R.id.plus_icon)?.setImageResource(R.drawable.minus)
        } else {
            convertView?.findViewById<ImageView>(R.id.plus_icon)?.setImageResource(R.drawable.plus)
        }
        titleDateTextView?.text = listTitle.date
        titleDayTextView?.text = "- " + listTitle.day

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
        fun onClick(listPosition: Int)
    }

}