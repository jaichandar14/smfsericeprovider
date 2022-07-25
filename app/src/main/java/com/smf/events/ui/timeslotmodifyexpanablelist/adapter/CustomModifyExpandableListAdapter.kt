package com.smf.events.ui.timeslotmodifyexpanablelist.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.R
import com.smf.events.helper.AppConstants
import com.smf.events.helper.CalendarUtils
import com.smf.events.ui.timeslotsexpandablelist.adapter.BookedSlotsRecyclerViewAdapter
import com.smf.events.ui.timeslotsexpandablelist.model.BookedEventsList
import com.smf.events.ui.timeslotsexpandablelist.model.ListDataModify
import java.time.LocalDate
import java.time.Month
import java.util.*
import kotlin.collections.ArrayList

class CustomModifyExpandableListAdapter internal constructor(
    private val context: Context,
    private val classTag: String,
    private val titleDate: ArrayList<String>,
    private val childData: HashMap<String, List<ListDataModify>>,
) : BaseExpandableListAdapter() {

    private val TAG = "CustomModifyExpandableL"
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
        parent: ViewGroup,
    ): View {
        var convertView = convertView
        val expandedListData = getChild(listPosition, expandedListPosition) as ListDataModify
        val layoutInflater =
            this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        convertView = if (expandedListPosition == 0) {
            layoutInflater.inflate(R.layout.time_slot_list, null)
        } else {
            layoutInflater.inflate(R.layout.time_slot_bottom, null)
        }
        val image12To3am = convertView?.findViewById<ImageView>(R.id.image_view_12_3am)
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
        if (expandedListData.timeSlot == "" && expandedListData.status[0].branchName == "") {
            layoutLinear12To3am?.visibility = View.INVISIBLE
            textNoEventsAvailable?.visibility = View.VISIBLE
        } else if (expandedListData.timeSlot == context.getString(R.string.empty)) {
            layoutLinear12To3am?.visibility = View.INVISIBLE
            textNoEventsAvailable?.visibility = View.INVISIBLE
            progressBar?.visibility = View.VISIBLE
        } else if (expandedListData.status.isEmpty()) {
            timeSlot12To3am?.text = expandedListData.timeSlot
            address12To3am?.text = context.getString(R.string.available)
            image12To3am?.setImageResource(R.drawable.new_selection)
            image12To3am?.setOnClickListener {
                Log.d(TAG, "getChildView: clicked $expandedListPosition")
                timeSlotIconOnClickListener?.onChildClick(
                    listPosition,
                    expandedListPosition,
                    expandedListData.timeSlot
                )
            }
        } else if (expandedListData.status[0].branchName == context.getString(R.string.available_small)) {
            timeSlot12To3am?.text = expandedListData.timeSlot
            address12To3am?.text = context.getString(R.string.available)
            image12To3am?.setImageResource(R.drawable.new_selection)
            image12To3am?.setOnClickListener {
                Log.d(TAG, "getChildView: clicked $expandedListPosition")
                timeSlotIconOnClickListener?.onChildClick(
                    listPosition,
                    expandedListPosition,
                    expandedListData.timeSlot
                )
            }
        } else if (expandedListData.status[0].branchName == context.getString(R.string.null_text)) {
            timeSlot12To3am?.text = expandedListData.timeSlot
            address12To3am?.text = context.getString(R.string.not_available)
            image12To3am?.setImageResource(R.drawable.unselect)
            image12To3am?.setOnClickListener {
                Log.d(TAG, "getChildView: clicked $expandedListPosition")
                timeSlotIconOnClickListener?.onChildClick(
                    listPosition,
                    expandedListPosition,
                    expandedListData.timeSlot
                )
            }
        } else {
            val list = ArrayList<BookedEventsList>()
            val statusList = ArrayList<String>()
            for (i in expandedListData.status.indices) {
                Log.d(TAG, "getChildView loop: ${expandedListData.status[i]}")
                list.add(
                    BookedEventsList(
                        dateFormat(expandedListData.status[i].eventDate),
                        expandedListData.status[i].eventName,
                        expandedListData.status[i].bidStatus
                    )
                )
                statusList.add(expandedListData.status[i].bidStatus)
            }
            Log.d(TAG, "getChildView expandedListData: $expandedListData")
            Log.d(TAG, "getChildView: $list")

            val finalEventsList = ArrayList<BookedEventsList>()
            if (statusList.contains(AppConstants.WON_BID)) {
                address12To3am?.text = context.getString(R.string.slot_booked)
                list.forEach {
                    if (it.bidStatus == AppConstants.WON_BID) {
                        finalEventsList.add(it)
                    }
                }
                image12To3am?.setOnClickListener {
                    Log.d(TAG, "getChildView: clicked $expandedListPosition")
                    timeSlotIconOnClickListener?.onChildClick(
                        listPosition,
                        expandedListPosition,
                        expandedListData.timeSlot
                    )
                }
            } else {
                address12To3am?.text = context.getString(R.string.Quote_Sent)
                finalEventsList.addAll(list)
                image12To3am?.setOnClickListener {
                    Log.d(TAG, "getChildView: clicked $expandedListPosition")
                    timeSlotIconOnClickListener?.onChildClick(
                        listPosition,
                        expandedListPosition,
                        expandedListData.timeSlot
                    )
                }
            }

            recyclerViewBookedSlot?.layoutManager = LinearLayoutManager(context)
            val bookedSlotsRecyclerViewAdapter = BookedSlotsRecyclerViewAdapter(finalEventsList)
            recyclerViewBookedSlot?.adapter = bookedSlotsRecyclerViewAdapter
            timeSlot12To3am?.text = expandedListData.timeSlot

        }

        // Loop For Last ChildView View Invisible
        if (isLastChild) {
            view12To3am?.visibility = View.GONE
        } else {
            view12To3am?.visibility = View.VISIBLE
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
        parent: ViewGroup,
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
        val titleLayoutInside =
            convertView?.findViewById<ConstraintLayout>(R.id.expandable_title_layout_inside)
        val expandableListLayout =
            convertView?.findViewById<LinearLayout>(R.id.expandable_title_layout)
        if (isExpanded) {
            convertView?.findViewById<ImageView>(R.id.plus_icon)?.setImageResource(R.drawable.minus)
        } else {
            convertView?.findViewById<ImageView>(R.id.plus_icon)?.setImageResource(R.drawable.plus)
        }
        titleDateTextView?.text = listTitle

        if (classTag == context.getString(R.string.day)) {
            dayClassTag(listPosition, titleLayoutInside, expandableListLayout, parent, isExpanded)
        } else if (classTag == context.getString(R.string.week)) {
            Log.d(TAG, "getGroupView: ${CalendarUtils.listOfDatesArray}, $titleDate")
            weekClassTag(listPosition, titleLayoutInside, expandableListLayout, parent, isExpanded)
        } else if (classTag == context.getString(R.string.month)) {
            monthClassTag(listPosition, titleLayoutInside, expandableListLayout, parent, isExpanded)
            Log.d(TAG, "getGroupView: ${CalendarUtils.allDaysList}, $listTitle")
        }

        return convertView!!
    }

    private fun dayClassTag(
        listPosition: Int,
        titleLayoutInside: ConstraintLayout?,
        expandableListLayout: LinearLayout?,
        parent: ViewGroup,
        isExpanded: Boolean,
    ) {
        val currentDayFormatter = CalendarUtils.dateFormatter
        try {
            val currentDay =
                LocalDate.parse(CalendarUtils.allDaysList[listPosition], currentDayFormatter)
            val businessValidationDate = CalendarUtils.businessValidity
            Log.d(TAG, "getGroupView : $businessValidationDate ${CalendarUtils.allDaysList}")
            if (currentDay > businessValidationDate) {
                titleLayoutInside?.setBackgroundResource(R.drawable.corner_radius_background_modify_slots_gray)
                expandableListLayout?.setOnClickListener {
                    timeSlotIconOnClickListener?.onGroupClick(
                        parent,
                        listPosition,
                        isExpanded,
                        true
                    )
                }
            } else {
                expandableListLayout?.setOnClickListener {
                    timeSlotIconOnClickListener?.onGroupClick(
                        parent,
                        listPosition,
                        isExpanded,
                        false
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "initializeExpandableListSetUp Exception adapter: $e")
        }
    }

    private fun weekClassTag(
        listPosition: Int,
        titleLayoutInside: ConstraintLayout?,
        expandableListLayout: LinearLayout?,
        parent: ViewGroup,
        isExpanded: Boolean,
    ) {
        CalendarUtils.listOfDatesArray[listPosition].forEach {
            val currentDay = LocalDate.parse(it, CalendarUtils.dateFormatter)
            val businessExpDate =
                CalendarUtils.businessValidity?.format(CalendarUtils.dateFormatter)
            val businessValidationDate =
                LocalDate.parse(businessExpDate, CalendarUtils.dateFormatter)
            if (currentDay > businessValidationDate) {
                titleLayoutInside?.setBackgroundResource(R.drawable.corner_radius_background_modify_slots_gray)
                expandableListLayout?.setOnClickListener {
                    timeSlotIconOnClickListener?.onGroupClick(
                        parent,
                        listPosition,
                        isExpanded,
                        true
                    )
                }
            } else {
                expandableListLayout?.setOnClickListener {
                    Log.d(TAG, "getGroupView :not called")
                    timeSlotIconOnClickListener?.onGroupClick(
                        parent,
                        listPosition,
                        isExpanded,
                        false
                    )
                }
            }
        }
    }

    private fun monthClassTag(
        listPosition: Int,
        titleLayoutInside: ConstraintLayout?,
        expandableListLayout: LinearLayout?,
        parent: ViewGroup,
        isExpanded: Boolean,
    ) {
        // save current month list of dates and use contains method
        val businessValidationDateLocalDate = CalendarUtils.businessValidity
        CalendarUtils.allDaysListForMonth.forEach {
            val currentDay = LocalDate.parse(it, CalendarUtils.dateFormatter)
            if (currentDay > businessValidationDateLocalDate) {
                titleLayoutInside?.setBackgroundResource(R.drawable.corner_radius_background_modify_slots_gray)
                expandableListLayout?.setOnClickListener {
                    timeSlotIconOnClickListener?.onGroupClick(
                        parent,
                        listPosition,
                        isExpanded,
                        true
                    )
                }
            } else {
                expandableListLayout?.setOnClickListener {
                    timeSlotIconOnClickListener?.onGroupClick(
                        parent,
                        listPosition,
                        isExpanded,
                        false
                    )
                }
            }
        }

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
        fun onChildClick(listPosition: Int, expandedListPosition: Int, timeSlot: String)
        fun onGroupClick(
            parent: ViewGroup,
            listPosition: Int,
            isExpanded: Boolean,
            businessValidationStatus: Boolean,
        )
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