package com.smf.events.ui.timeslot

import android.content.Context
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
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import javax.inject.Inject

// 2487
class TimeSlotsFragment : Fragment() {

    private var TAG = "TimeSlotsFragment"
    lateinit var tabLayout: TabLayout
    private lateinit var mDataBinding: FragmentTimeSlotsBinding
    private lateinit var tabVisibilityDisposable: Disposable

    // SharedViewModel Initialization
    private val sharedViewModel: ScheduleManagementViewModel by activityViewModels()

    @Inject
    lateinit var getTimeSlotFragments: GetTimeSlotFragments

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

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
        // Setting Time slot visibility
        timeSlotVisibility()
        // 2527 - tabLayout And ViewPager Initialization
        tabLayoutAndViewPagerSetUp()
        // 2843 - Initial Tab Position
        updatePosition()
    }

    private fun timeSlotVisibility() {
        if (tag == "Frag_Bottom_tag") {
            mDataBinding.timeSlotLayout.visibility = View.GONE
        } else {
            mDataBinding.timeSlotLayout.visibility = View.VISIBLE
        }
        // Observer for visible timeSlotLayout after calendar visibility
        tabVisibilityDisposable = RxBus.listen(RxEvent.IsValid::class.java).subscribe {
            mDataBinding.timeSlotLayout.visibility = View.VISIBLE
        }
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
        // 3204 - Getting Selected Fragment
        val frg = getTimeSlotFragments.getSlot(tag, requireContext())
        val manager: FragmentManager =
            requireActivity().supportFragmentManager //create an instance of fragment manager
        val transaction: FragmentTransaction =
            manager.beginTransaction() //create an instance of Fragment-transaction
        transaction.replace(R.id.expandable_fragment, frg, status)
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!tabVisibilityDisposable.isDisposed) tabVisibilityDisposable.dispose()
    }

}