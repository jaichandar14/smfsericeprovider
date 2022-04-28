package com.smf.events.ui.timeslot.adapter

import android.content.res.Resources
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.smf.events.ui.timeslotsexpandablelist.TimeSlotsExpandableListFragment

class TimeSlotViewPagerAdapter(
    fragmentManager: FragmentManager, lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TimeSlotsExpandableListFragment()
            1 -> TimeSlotsExpandableListFragment()
            2 -> TimeSlotsExpandableListFragment()
            else -> {
                throw Resources.NotFoundException("Position Not Found")
            }
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

}