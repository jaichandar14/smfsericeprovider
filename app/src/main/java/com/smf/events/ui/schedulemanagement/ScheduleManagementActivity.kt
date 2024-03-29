package com.smf.events.ui.schedulemanagement

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseActivity
import com.smf.events.databinding.ActivityScheduleManagmentBinding
import com.smf.events.helper.CalendarUtils
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

    var TAG = this::class.java.name
    private var status = false

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var changingNavDisposable: Disposable
    private lateinit var internetDisposable: Disposable
    private lateinit var toggleBtnVisibilityDisposable: Disposable
    override fun getContentView(): Int = R.layout.activity_schedule_managment

    override fun getViewModel(): ScheduleManagementViewModel =
        ViewModelProvider(this, factory)[ScheduleManagementViewModel::class.java]

    override fun getBindingVariable(): Int = BR.scheduleManagementViewModel

    @SuppressLint("ResourceAsColor", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        super.onPostCreate(savedInstanceState, null)
        // Set Status bar
        setStatusBarColor()
        // 2458 Method for Calendar Ui
        calendarUI()
        // 2458 Method for TimeSlots Ui
        timeSlotsUI()
        // 2842 Setting the updatedTabPosition when we press back button
        CalendarUtils.updatedTabPosition = 0
        changingNavDisposable = RxBus.listen(RxEvent.ChangingNav::class.java).subscribe {
            finish()
        }

        // 2904 Refresh the Schedule management layout
        mViewDataBinding?.refreshLayout?.setOnRefreshListener {
            // Your code to refresh the list here.
            finish()
            startActivity(intent)
            mViewDataBinding?.refreshLayout?.isRefreshing = true
        }

        internetDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer scheduled activity")
        }

        // Observer for visible switch button and text
        toggleBtnVisibilityDisposable = RxBus.listen(RxEvent.IsValid::class.java).subscribe {
            mViewDataBinding?.switchBtnTx?.visibility = View.VISIBLE
            mViewDataBinding?.switchBtn?.visibility = View.VISIBLE
        }

        getViewModel().getScrollViewToPosition.observe(this, Observer {
            var totalHeaderHeight =
                mViewDataBinding!!.calendarFragment.height + mViewDataBinding!!.switchBtnTx.height + mViewDataBinding!!.switchBtn.height
            totalHeaderHeight += it
            mViewDataBinding!!.scrollView.smoothScrollTo(0, totalHeaderHeight)
        })
    }

    // 2458 - Method for Calendar Ui
    fun calendarUI() {
        // 2528 - Toggle Button Logic
        mViewDataBinding?.switchBtn?.setOnClickListener {
            if (mViewDataBinding?.switchBtn?.isChecked == false)
                mViewDataBinding?.switchBtnTx?.text =
                    resources.getString(com.smf.events.R.string.switch_to_modify_slots_availability)
            else mViewDataBinding?.switchBtnTx?.text =
                resources.getString(R.string.switch_to_View_Event_List)

            status = mViewDataBinding?.switchBtn?.isChecked != false
            Log.d("TAG", "calendarUI: $status")
            updateTimeSlotsUI(status)
//            } else {
//                mViewDataBinding?.switchBtn?.isChecked =
//                    mViewDataBinding?.switchBtn?.isChecked != true
//            }
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
    fun timeSlotsUI() {
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
        transaction.replace(R.id.timeslots_fragment, frg, status.toString())
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onViewCreated: observe onDestroy: called scheduled activity")
        if (changingNavDisposable.isDisposed.not()) changingNavDisposable.dispose()
        if (internetDisposable.isDisposed.not()) internetDisposable.dispose()
        if (toggleBtnVisibilityDisposable.isDisposed.not()) toggleBtnVisibilityDisposable.dispose()
    }

}