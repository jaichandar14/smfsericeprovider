package com.smf.events.ui.timeslot

import android.content.Context
import androidx.fragment.app.Fragment
import com.smf.events.R
import com.smf.events.helper.CalendarUtils
import com.smf.events.ui.timeslotmodifyexpanablelist.DayModifyExpandableListFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.MonthModifyExpandableListFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.WeekModifyExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.DayExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.MonthExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.WeekExpandableListFragment
import javax.inject.Inject

class GetTimeSlotFragments @Inject constructor() {

    fun getSlot(tag: String?, context: Context): Fragment {
        return when (CalendarUtils.updatedTabPosition) {
            0 -> {
                if (tag == context.getString(R.string.trueText)) {
                    DayModifyExpandableListFragment()
                } else {
                    DayExpandableListFragment()
                }
            }
            1 -> {
                if (tag == context.getString(R.string.trueText)) {
                    WeekModifyExpandableListFragment()
                } else {
                    WeekExpandableListFragment()
                }
            }
            2 -> {
                if (tag == context.getString(R.string.trueText)) {
                    MonthModifyExpandableListFragment()
                } else {
                    MonthExpandableListFragment()
                }
            }
            else -> {
                DayExpandableListFragment()
            }
        }
    }

}