package com.smf.events.ui.timeslot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.smf.events.R
import com.smf.events.databinding.FragmentTimeSlotsBinding
import com.smf.events.helper.CalendarUtils
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslot.adapter.TimeSlotViewPagerBookedEventsAdapter
import com.smf.events.ui.timeslot.adapter.TimeSlotViewPagerModifyAvailabilityAdapter

// 2487
class TimeSlotsFragment : Fragment() {

    private var TAG = "TimeSlotsFragment"
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    private lateinit var mDataBinding: FragmentTimeSlotsBinding

    // SharedViewModel Initialization
    private val sharedViewModel: ScheduleManagementViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mDataBinding = FragmentTimeSlotsBinding.inflate(inflater, container, false)
        return mDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2527 - Initialize tabLayout
        tabLayout = mDataBinding.tabLayout
        // 2527 - Initialize viewPager
        viewPager = mDataBinding.viewPager
        // 2527 - tabLayout And ViewPager Initialization
        tabLayoutAndViewPagerSetUp()
        // 2843 - Initial Tab Position
        updatePosition()
    }

    private fun tabLayoutAndViewPagerSetUp() {
        // 2527 - Set Data For TabLayout
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.day)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.week)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.month)))
        // 2527 - TabLayout Page Limit
        viewPager.offscreenPageLimit = 3
        if (tag == "true") {
            // Set View Pager Adapter
            viewPager.adapter =
                TimeSlotViewPagerModifyAvailabilityAdapter(
                    requireActivity().supportFragmentManager,
                    lifecycle
                )
        } else {
            // Set View Pager Adapter
            viewPager.adapter =
                TimeSlotViewPagerBookedEventsAdapter(
                    requireActivity().supportFragmentManager,
                    lifecycle
                )
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                CalendarUtils.updatedTabPosition = tab!!.position
                updatePosition()
                when (tab.position) {
                    0 -> sharedViewModel.setCalendarFormat(getString(R.string.day))
                    1 -> sharedViewModel.setCalendarFormat(getString(R.string.week))
                    2 -> sharedViewModel.setCalendarFormat(getString(R.string.month))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        // 2843 - Disable ViewPager Swipe
        viewPager.isUserInputEnabled = false
    }

    // 2843 - Method For Set Tab Position
    private fun updatePosition() {
        Log.d(TAG, "updatePosition: inside called ${CalendarUtils.updatedTabPosition}")
        tabLayout.selectTab(tabLayout.getTabAt(CalendarUtils.updatedTabPosition))
        viewPager.post {
            viewPager.setCurrentItem(CalendarUtils.updatedTabPosition, true)
        }
    }

}