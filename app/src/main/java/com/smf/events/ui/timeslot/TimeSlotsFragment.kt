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
import com.smf.events.databinding.FragmentTimeSlotsBinding
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslot.adapter.TimeSlotViewPagerAdapter

// 2487
class TimeSlotsFragment : Fragment() {

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
        // 2558 - getDate ScheduleManagementViewModel Observer
        sharedViewModel.getDate.observe(requireActivity(), {
            Log.d("TAG", "onCreateView viewModel called TimeSlotsFragment: $it")
        })

    }

    private fun tabLayoutAndViewPagerSetUp() {
        // 2527 - Set Data For TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Day(2)"))
        tabLayout.addTab(tabLayout.newTab().setText("Week(4)"))
        tabLayout.addTab(tabLayout.newTab().setText("Month(6)"))
        // 2527 - TabLayout Page Limit
        viewPager.offscreenPageLimit = 3
        // Set View Pager Adapter
        viewPager.adapter =
            TimeSlotViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
    }

}