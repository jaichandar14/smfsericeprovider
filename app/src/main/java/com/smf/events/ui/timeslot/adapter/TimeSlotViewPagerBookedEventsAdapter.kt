package com.smf.events.ui.timeslot.adapter

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.smf.events.ui.timeslotsexpandablelist.DayExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.MonthExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.WeekExpandableListFragment

class TimeSlotViewPagerBookedEventsAdapter(
    fragmentManager: FragmentManager, lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DayExpandableListFragment()
            1 -> WeekExpandableListFragment()
            2 -> MonthExpandableListFragment()
            else -> {
                throw Resources.NotFoundException("Position Not Found")
            }
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

}