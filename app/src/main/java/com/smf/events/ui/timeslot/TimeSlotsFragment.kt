package com.smf.events.ui.timeslot

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentTimeSlotsBinding
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslot.adapter.TimeSlotViewPagerAdapter
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

// 2487
class TimeSlotsFragment :
    BaseFragment<FragmentTimeSlotsBinding, ScheduleManagementViewModel>() {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): ScheduleManagementViewModel =
        ViewModelProvider(this, factory).get(ScheduleManagementViewModel::class.java)

    override fun getBindingVariable(): Int = BR.timeslotsfragmentViewModel

    override fun getContentView(): Int = R.layout.fragment_time_slots

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2527 - Initialize tabLayout
        tabLayout = mDataBinding?.tabLayout!!
        // 2527 - Initialize viewPager
        viewPager = mDataBinding?.viewPager!!
        // 2527 - tabLayout And ViewPager Initialization
        tabLayoutAndViewPagerSetUp()

    }

    private fun  tabLayoutAndViewPagerSetUp(){
        // 2527 - Set Data For TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Day(2)"))
        tabLayout.addTab(tabLayout.newTab().setText("Week(4)"))
        tabLayout.addTab(tabLayout.newTab().setText("Month(6)"))
        // 2527 - TabLayout Page Limit
        viewPager.offscreenPageLimit = 3
        // Set View Pager Adapter
        viewPager.adapter= TimeSlotViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle)

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