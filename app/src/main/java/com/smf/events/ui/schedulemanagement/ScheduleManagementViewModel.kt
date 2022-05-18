package com.smf.events.ui.schedulemanagement

import android.annotation.SuppressLint
import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.smf.events.base.BaseViewModel
import com.smf.events.databinding.FragmentCalendarBinding
import kotlinx.coroutines.Dispatchers
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject


class ScheduleManagementViewModel @Inject constructor(
    private val scheduleManagementRepository: ScheduleManagementRepository,
    application: Application,
) : BaseViewModel(application) {

    var name: String? = null
    var allServiceposition: Int? = 0
    private val now: LocalDate = LocalDate.now()
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    // 2670 - CurrentDate LiveData
    private var currentDate = MutableLiveData(now.format(dateFormatter))
    val getCurrentDate: LiveData<String> = currentDate
    fun setCurrentDate(newDate: String) {
        currentDate.value = newDate
    }

    // 2670 - StartOfCurrentWeekDate LiveData
    private val firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    private val startOfCurrentWeek: LocalDate =
        now.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    private var startOfCurrentWeekDate = MutableLiveData(startOfCurrentWeek.format(dateFormatter))
    val getStartOfCurrentWeekDate: LiveData<String> = startOfCurrentWeekDate
    fun setStartOfCurrentWeekDate(newDate: String) {
        startOfCurrentWeekDate.value = newDate
    }

    // 2670 - EndOfWeekDate LiveData
    private val lastDayOfWeek: DayOfWeek = firstDayOfWeek.plus(6)
    private val endOfWeek: LocalDate = now.with(TemporalAdjusters.nextOrSame(lastDayOfWeek))
    private var endOfWeekDate = MutableLiveData(endOfWeek.format(dateFormatter))
    val getEndOfWeekDate: LiveData<String> = endOfWeekDate
    fun setEndOfWeekDate(newDate: String) {
        endOfWeekDate.value = newDate
    }

    // 2670 - MonthBeginDate LiveData
    private val monthBegin: LocalDate = LocalDate.now().withDayOfMonth(1)
    private var monthBeginDate = MutableLiveData(monthBegin.format(dateFormatter))
    val getMonthBeginDate: LiveData<String> = monthBeginDate
    fun setMonthBeginDate(newDate: String) {
        monthBeginDate.value = newDate
    }

    // 2670 - MonthEndDate LiveData
    private val monthEnd: LocalDate = LocalDate.now().plusMonths(1).withDayOfMonth(1).minusDays(1)
    private var monthEndDate = MutableLiveData(monthEnd.format(dateFormatter))
    val getMonthEndDate: LiveData<String> = monthEndDate
    fun setMonthEndDate(newDate: String) {
        monthEndDate.value = newDate
    }

    // 2622 Mutable live data to get the Calendar Format
    private var calendarFormat = MutableLiveData<String>()
    val getCalendarFormat: LiveData<String> = calendarFormat
    fun setCalendarFormat(mCalFormat : String){
        calendarFormat.value = mCalFormat
    }
    // 2458 Method for Setting All Service
    @SuppressLint("ResourceType")
    fun allServices(
        mViewDataBinding: FragmentCalendarBinding?,
        allServiceList: ArrayList<String>,
    ) {
        mViewDataBinding?.spnAllServices?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long,
                ) {
                    allServiceposition = position
                    callBackInterface?.itemClick(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        val arrayAdapter: ArrayAdapter<Any?> = ArrayAdapter(
            getApplication(),
            com.smf.events.R.layout.spinners_list,
            allServiceList as List<Any?>
        )
        arrayAdapter.setDropDownViewResource(com.smf.events.R.layout.spinners_list)
        arrayAdapter.notifyDataSetChanged()
        mViewDataBinding?.spnAllServices?.adapter = arrayAdapter
    }

    // 2458 Method for  Setting Branches
    @SuppressLint("ResourceType")
    fun branches(
        mDataBinding: FragmentCalendarBinding?,
        branchData: java.util.ArrayList<String>,
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int,
        i: Int,
    ) {
        var spinner = mDataBinding!!.spnBranches
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                callBackInterface?.branchItemClick(position, name, allServiceposition)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val arrayAdapter: ArrayAdapter<Any?> = ArrayAdapter(
            getApplication(),
            com.smf.events.R.layout.spinners_list,
            branchData as List<Any?>
        )
        arrayAdapter.setDropDownViewResource(com.smf.events.R.layout.spinners_list)
        arrayAdapter.notifyDataSetChanged()
        mDataBinding.spnBranches.adapter = arrayAdapter
    }

    // 2458 Method for  Setting Years
    @SuppressLint("ResourceType")
    fun year(
        mDataBinding: FragmentCalendarBinding?,
        calendarUtils: ArrayList<String>,
    ) {
        var spinner = mDataBinding!!.monthYearspn
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                allServiceposition = position
                // callBackInterface?.itemClick(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val arrayAdapter: ArrayAdapter<Any?> = ArrayAdapter(
            getApplication(),
            com.smf.events.R.layout.spinners_list,
            calendarUtils as List<Any?>
        )
        arrayAdapter.setDropDownViewResource(com.smf.events.R.layout.spinners_list)
        spinner.adapter = arrayAdapter
        arrayAdapter.notifyDataSetChanged()
        mDataBinding.monthYearspn.adapter = arrayAdapter
    }

    // 2458 Method For Getting All Service
    fun getAllServices(idToken: String, spRegId: Int) = liveData(Dispatchers.IO) {
        emit(scheduleManagementRepository.getAllServices(idToken, spRegId))
    }

    // 2458 Method For Getting Branches
    fun getServicesBranches(idToken: String, spRegId: Int, serviceCategoryId: Int) =
        liveData(Dispatchers.IO) {
            emit(
                scheduleManagementRepository.getServicesBranches(
                    idToken,
                    spRegId,
                    serviceCategoryId
                )
            )
        }

    // 2670 - Method For Get Booked Event Services
    fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String
    ) =
        liveData(Dispatchers.IO) {
            emit(
                scheduleManagementRepository.getBookedEventServices(
                    idToken,
                    spRegId,
                    serviceCategoryId,
                    serviceVendorOnBoardingId,
                    fromDate,
                    toDate
                )
            )
        }

    // 2622 EventDates for Calendar Api
    fun getEventDates(idToken: String, spRegId: Int, serviceCategoryId: Int?,
                      serviceVendorOnboardingId:Int?,
                      fromDate:String,
                      toDate:String,) =
        liveData(Dispatchers.IO) {
            emit(scheduleManagementRepository.getEventDates(idToken,
                spRegId,
                serviceCategoryId,serviceVendorOnboardingId,fromDate,toDate))
        }

    private var callBackInterface: CallBackInterface? = null

    // 2458 Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // 2458 CallBack Interface
    interface CallBackInterface {
        fun itemClick(msg: Int)
        fun branchItemClick(serviceVendorOnboardingId: Int, name: String?, allServiceposition: Int?)
    }
}