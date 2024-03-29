package com.smf.events.ui.timeslotsexpandablelist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.ui.timeslotsexpandablelist.model.BookedEventsList
import com.smf.events.ui.timeslotsexpandablelist.model.ListData
import java.time.Month
import java.util.*

class CustomExpandableListAdapter internal constructor(
    private val context: Context,
    private val classTag: String,
    private val titleDate: ArrayList<String>,
    private val childData: HashMap<String, List<ListData>>
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.childData[this.titleDate[listPosition]]!![expandedListPosition]
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.childData[this.titleDate[listPosition]]!!.size
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
        val timeSlot12To3am = convertView?.findViewById<TextView>(R.id.time_slot_12_3am)
        val address12To3am = convertView?.findViewById<TextView>(R.id.address_12_3am)
        val view12To3am = convertView?.findViewById<View>(R.id.view_12_3am)
        val layoutLinear12To3am = convertView?.findViewById<View>(R.id.layout_linear_12_3am)
        val textNoEventsAvailable = convertView?.findViewById<View>(R.id.text_no_events_available)
        val progressBar = convertView?.findViewById<ProgressBar>(R.id.progress_bar_child)
        val recyclerViewBookedSlot =
            convertView?.findViewById<RecyclerView>(R.id.slot_list_recycler_view)
        address12To3am?.text = null

        // Verification For Booked Events Data
        if (expandedListData.timeSlot == "") {
            layoutLinear12To3am?.visibility = View.INVISIBLE
            textNoEventsAvailable?.visibility = View.VISIBLE
        } else if (expandedListData.timeSlot == context.getString(R.string.empty)) {
            layoutLinear12To3am?.visibility = View.INVISIBLE
            textNoEventsAvailable?.visibility = View.INVISIBLE
            progressBar?.visibility = View.VISIBLE
        } else {
            val list = ArrayList<BookedEventsList>().apply {
                for (i in expandedListData.status.indices) {
                    this.add(
                        BookedEventsList(
                            dateFormat(expandedListData.status[i].eventDate),
                            expandedListData.status[i].eventName, expandedListData.status[i].bidStatus
                        )
                    )
                }
            }
            recyclerViewBookedSlot?.layoutManager = LinearLayoutManager(context)
            val bookedSlotsRecyclerViewAdapter = BookedSlotsRecyclerViewAdapter(list)
            recyclerViewBookedSlot?.adapter = bookedSlotsRecyclerViewAdapter
            address12To3am?.text = context.getString(R.string.slot_booked)
            timeSlot12To3am?.text = expandedListData.timeSlot

            // Loop For Last ChildView View Invisible
            if (isLastChild) {
                view12To3am?.visibility = View.GONE
            } else {
                view12To3am?.visibility = View.VISIBLE
            }
        }

        return convertView!!
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleDate[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleDate.size
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
        val expandableListLayout =
            convertView?.findViewById<LinearLayout>(R.id.expandable_title_layout)
        if (isExpanded) {
            convertView?.findViewById<ImageView>(R.id.plus_icon)?.setImageResource(R.drawable.minus)
        } else {
            convertView?.findViewById<ImageView>(R.id.plus_icon)?.setImageResource(R.drawable.plus)
        }
        titleDateTextView?.text = listTitle

        expandableListLayout?.setOnClickListener {
            timeSlotIconOnClickListener?.onGroupClick(parent, listPosition, isExpanded)
        }

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
        fun onGroupClick(parent: ViewGroup, listPosition: Int, isExpanded: Boolean)
    }

    // 2670 - Method For Date And Month Arrangement To Display UI
    private fun dateFormat(input: String): String {
        var monthCount = input.substring(0, 2)
        val date = input.substring(3, 5)
        if (monthCount[0].digitToInt() == 0) {
            monthCount = monthCount[1].toString()
        }
        val month = Month.of(monthCount.toInt()).toString().substring(0, 3).let { month ->
            month.substring(0, 1) + month.substring(1, 2)
                .lowercase(Locale.getDefault()) + month.substring(2, 3)
                .lowercase(Locale.getDefault())
        }
        return "$month $date"
    }
}