package com.smf.events.ui.schedulemanagement

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseActivity
import com.smf.events.databinding.ActivityScheduleManagmentBinding
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.schedulemanagement.calendarfragment.CalendarFragment
import com.smf.events.ui.timeslot.TimeSlotsFragment
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import javax.inject.Inject

// 2458
class ScheduleManagementActivity :
    BaseActivity<ActivityScheduleManagmentBinding, ScheduleManagementViewModel>() {

    private var status = false

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var dialogDisposable: Disposable
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
        dialogDisposable = RxBus.listen(RxEvent.ChangingNav::class.java).subscribe {
            finish()
        }
    }

    // 2458 - Method for Calendar Ui
    private fun calendarUI() {
        // 2528 - Toggle Button Logic
        mViewDataBinding?.switchBtn?.setOnClickListener {
            if (mViewDataBinding?.switchBtn?.isChecked == false)
                mViewDataBinding?.switchBtnTx?.text =
                    resources.getString(R.string.switch_to_modify_slots_availability)
            else mViewDataBinding?.switchBtnTx?.text =
                resources.getString(R.string.switch_to_View_Event_List)

            status = mViewDataBinding?.switchBtn?.isChecked != false
            Log.d("TAG", "calendarUI: $status")
            updateTimeSlotsUI(status)
        }
        Log.d("TAG", "calendarUI: $status")
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

    // 2527 - Method for TimeSlots Ui
    private fun updateTimeSlotsUI(status: Boolean) {
        val frg = TimeSlotsFragment() //create the fragment instance for the middle fragment
        val manager: FragmentManager =
            supportFragmentManager //create an instance of fragment manager
        val transaction: FragmentTransaction =
            manager.beginTransaction() //create an instance of Fragment-transaction
        if (status) {
            Log.d("TAG", "calendarUI if : $status")
            transaction.replace(R.id.timeslots_fragment, frg, status.toString())
        } else {
            Log.d("TAG", "calendarUI else: $status")
            transaction.replace(R.id.timeslots_fragment, frg, status.toString())
        }
        transaction.commit()
    }
}