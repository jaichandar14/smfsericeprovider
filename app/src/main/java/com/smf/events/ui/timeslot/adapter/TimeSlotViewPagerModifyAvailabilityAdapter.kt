package com.smf.events.ui.timeslot.adapter

import android.content.res.Resources
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.smf.events.ui.timeslotmodifyexpanablelist.DayModifyExpandableListFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.MonthModifyExpandableListFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.WeekModifyExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.DayExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.MonthExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.WeekExpandableListFragment

class TimeSlotViewPagerModifyAvailabilityAdapter (
    fragmentManager: FragmentManager, lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        Log.d("TAG", "calendarUI tag: called")
        return when (position) {
            0 -> DayModifyExpandableListFragment()
            1 -> WeekModifyExpandableListFragment()
            2 -> MonthModifyExpandableListFragment()
            else -> {
                throw Resources.NotFoundException("Position Not Found")
            }
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

}