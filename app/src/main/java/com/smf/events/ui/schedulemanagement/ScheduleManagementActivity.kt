package com.smf.events.ui.schedulemanagement

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseActivity
import com.smf.events.databinding.ActivityScheduleManagmentBinding
import com.smf.events.ui.schedulemanagement.calendarfragment.CalendarFragment
import com.smf.events.ui.timeslot.TimeSlotsFragment
import dagger.android.AndroidInjection
import javax.inject.Inject

// 2458
class ScheduleManagementActivity :
    BaseActivity<ActivityScheduleManagmentBinding, ScheduleManagementViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getContentView(): Int = R.layout.activity_schedule_managment

    override fun getViewModel(): ScheduleManagementViewModel =
        ViewModelProvider(this, factory).get(ScheduleManagementViewModel::class.java)

    override fun getBindingVariable(): Int = BR.scheduleManagementViewModel

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        // 2458 Method for Calendar Ui
        calendarUI()
        // 2458 Method for TimeSlots Ui
        timeSlotsUI()
    }

    // 2458 - Method for Calendar Ui
    private fun calendarUI() {
        val frg = CalendarFragment() //create the fragment instance for the middle fragment
        val manager: FragmentManager =
            supportFragmentManager //create an instance of fragment manager
        val transaction: FragmentTransaction =
            manager.beginTransaction() //create an instance of Fragment-transaction
        transaction.add(R.id.calendar_fragment, frg, "Frag_Top_tag")
        transaction.commit()
    }

    // 2527 - Method for TimeSlots Ui
    private fun timeSlotsUI() {
        val frg = TimeSlotsFragment() //create the fragment instance for the middle fragment
        val manager: FragmentManager =
            supportFragmentManager //create an instance of fragment manager
        val transaction: FragmentTransaction =
            manager.beginTransaction() //create an instance of Fragment-transaction
        transaction.add(R.id.timeslots_fragment, frg, "Frag_Bottom_tag")
        transaction.commit()
    }
}