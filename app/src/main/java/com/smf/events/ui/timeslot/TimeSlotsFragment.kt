package com.smf.events.ui.timeslot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayout
import com.smf.events.R
import com.smf.events.databinding.FragmentTimeSlotsBinding
import com.smf.events.helper.CalendarUtils
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslotmodifyexpanablelist.DayModifyExpandableListFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.MonthModifyExpandableListFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.WeekModifyExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.DayExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.MonthExpandableListFragment
import com.smf.events.ui.timeslotsexpandablelist.WeekExpandableListFragment

// 2487
class TimeSlotsFragment : Fragment() {

    private var TAG = "TimeSlotsFragment"
    lateinit var tabLayout: TabLayout
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
    }

    // 2843 - Method For Set Tab Position
    private fun updatePosition() {
        Log.d(TAG, "updatePosition: inside called ${CalendarUtils.updatedTabPosition}")
        tabLayout.selectTab(tabLayout.getTabAt(CalendarUtils.updatedTabPosition))
        tag?.let { updateTimeSlotsUI(it) }
    }

    private fun updateTimeSlotsUI(status: String) {
        val frg = when (CalendarUtils.updatedTabPosition) {
            0 -> {
                if (tag == "true") {
                    DayModifyExpandableListFragment()
                } else {
                    DayExpandableListFragment()
                }
            }
            1 -> {
                if (tag == "true") {
                    WeekModifyExpandableListFragment()
                } else {
                    WeekExpandableListFragment()
                }
            }
            2 -> {
                if (tag == "true") {
                    MonthModifyExpandableListFragment()
                } else {
                    MonthExpandableListFragment()
                }
            }
            else -> {
                DayExpandableListFragment()
            }
        }
        val manager: FragmentManager =
            requireActivity().supportFragmentManager //create an instance of fragment manager
        val transaction: FragmentTransaction =
            manager.beginTransaction() //create an instance of Fragment-transaction
        transaction.replace(R.id.expandable_fragment, frg, status)
        transaction.commit()
    }

}