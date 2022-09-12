package com.smf.events.ui.schedulemanagement

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smf.events.base.BaseViewModel
import com.smf.events.databinding.FragmentCalendarBinding
import com.smf.events.helper.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject

class ScheduleManagementViewModel @Inject constructor(
    private val scheduleManagementRepository: ScheduleManagementRepository,
    application: Application,
) : BaseViewModel(application) {

    var name: String? = null
    var allServiceposition: Int? = 0

    // 2735
    // 2670 - CurrentDate LiveData
    data class SelectedDate(
        var selectedDate: String,
        var seviceId: Int,
        var branchId: Int,
        var listOfDays: ArrayList<String>,
        var allDaysList: ArrayList<String>,
        var isScroll: Boolean
    )

    private var scrollViewToPosition = MutableLiveData<Int>()
    fun setScrollViewToPosition(scrollViewToPosition: Int) {
        this.scrollViewToPosition.postValue(scrollViewToPosition)
    }

    val getScrollViewToPosition: LiveData<Int> = scrollViewToPosition

    private var currentDate = MutableLiveData<SelectedDate>()
    val getCurrentDate: LiveData<SelectedDate> = currentDate
    fun setCurrentDate(
        selectedDate: String,
        seviceId: Int,
        branchId: Int,
        listOfDays: ArrayList<String>,
        allDaysList: ArrayList<String>,
        isScroll: Boolean
    ) {
        currentDate.value =
            SelectedDate(selectedDate, seviceId, branchId, listOfDays, allDaysList, isScroll)
        Log.d("TAG", "setCurrentDate: $allDaysList")
    }

    // 2795 - From TimeSlot UI Expanded Date Send To Calendar UI
    private var currentDateExp = MutableLiveData<String>()
    val getExpCurrentDate: LiveData<String> = currentDateExp
    fun setExpCurrentDate(
        currentDate: String,
    ) {
        currentDateExp.value = currentDate
        Log.d("TAG", "setCurrentDate currentDate: $currentDate")
    }

    // 2795 - From TimeSlot UI Expanded Week Send To Calendar UI
    private var currentWeekExp = MutableLiveData<ArrayList<String>>()
    val getExpCurrentWeek: LiveData<ArrayList<String>> = currentWeekExp
    fun setExpCurrentWeek(
        currentDateWeek: ArrayList<String>,
    ) {
        currentWeekExp.value = currentDateWeek
        Log.d("TAG", "setCurrentDate currentWeek: $currentDateWeek")
    }

    // 2686 Week from and to Date Live Data
    private var weekDate = MutableLiveData<WeekDates>()

    data class WeekDates(
        var weekListMapOfMonth: HashMap<Int, com.smf.events.helper.WeekDatesOfMonth>,
        var seviceId: Int,
        var branchId: Int,
        var weekList: ArrayList<String>,
        var bookedWeekList: ArrayList<String>,
        var isScroll: Boolean
    )

    val getCurrentWeekDate: LiveData<WeekDates> = weekDate
    fun setCurrentWeekDate(
        weekListMapOfMonth: HashMap<Int, com.smf.events.helper.WeekDatesOfMonth>,
        serviceId: Int,
        branchId: Int,
        weekList: ArrayList<String>,
        bookedWeekList: ArrayList<String>,
        isScroll: Boolean

    ) {
        Log.d("TAG", "setCurrentWeekDate: $weekListMapOfMonth  ")
        weekDate.value =
            WeekDates(weekListMapOfMonth, serviceId, branchId, weekList, bookedWeekList, isScroll)
    }

    // 2686 Month From and To Date Live Data
    private var monthDates = MutableLiveData<MonthDates>()

    data class MonthDates(
        var fromDate: String,
        var toDate: String,
        var currentDate: String,
        var monthValue: Int,
        var seviceId: Int,
        var branchId: Int,
        var monthFromAndToDate: ArrayList<String>,
    )

    val getCurrentMonthDate: LiveData<MonthDates> = monthDates
    fun setCurrentMonthDate(
        fromDate: String,
        toDate: String,
        currentDate: String,
        monthValue: Int,
        serviceId: Int, branchId: Int, monthFromAndToDate: ArrayList<String>
    ) {
        Log.d("TAG", "setCurrentMonthDate: $fromDate  $toDate")
        monthDates.value =
            MonthDates(
                fromDate,
                toDate,
                currentDate,
                monthValue,
                serviceId,
                branchId,
                monthFromAndToDate
            )
    }

    // 2622 Mutable live data to get the Calendar Format
    private var calendarFormat = MutableLiveData<String>()
    val getCalendarFormat: LiveData<String> = calendarFormat
    fun setCalendarFormat(mCalFormat: String) {
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
        branchData: ArrayList<String>,
    ) {
        val spinner = mDataBinding!!.spnBranches
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

    // 2458 Method For Getting All Service
    fun getAllServices(idToken: String, spRegId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(scheduleManagementRepository.getAllServices(idToken, spRegId))
        } catch (e: Exception) {
            Log.d("TAG", "getBookedEventServices mody: $e")
            when (e) {
                is UnknownHostException -> {
                    Log.d("TAG", "getBookedEventServices when mody called: $e")
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
                is ConnectException -> {
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
            }
        }
    }

    // 2985 Method For Getting BusinessValidity
    fun getBusinessValiditiy(idToken: String, spRegId: Int) = liveData(Dispatchers.IO) {
        try {
            emit(scheduleManagementRepository.getBusinessValiditiy(idToken, spRegId))
        } catch (e: Exception) {
            Log.d("TAG", "getBookedEventServices mody: $e")
            when (e) {
                is UnknownHostException -> {
                    Log.d("TAG", "getBookedEventServices when mody called: $e")
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
                is ConnectException -> {
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
            }
        }
    }

    // 2458 Method For Getting Branches
    fun getServicesBranches(idToken: String, spRegId: Int, serviceCategoryId: Int) =
        liveData(Dispatchers.IO) {
            try {
                emit(
                    scheduleManagementRepository.getServicesBranches(
                        idToken,
                        spRegId,
                        serviceCategoryId
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is UnknownHostException -> {
                        Log.d("TAG", "getBookedEventServices when mody called: $e")
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    is ConnectException -> {
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                }
            }
        }

    // 2622 EventDates for Calendar Api
    fun getEventDates(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnboardingId: Int?,
        fromDate: String,
        toDate: String,
    ) =
        liveData(Dispatchers.IO) {
            try {
                emit(
                    scheduleManagementRepository.getEventDates(
                        idToken,
                        spRegId,
                        serviceCategoryId, serviceVendorOnboardingId, fromDate, toDate
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is UnknownHostException -> {
                        Log.d("TAG", "getBookedEventServices when mody called: $e")
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    is ConnectException -> {
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                }
            }
        }

    // 2670 - Method For Get Booked Event Services
    fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String, caller: String
    ) =
        liveData(Dispatchers.IO) {
            try {
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
            } catch (e: Exception) {
                Log.d("TAG", "getBookedEventServices: $e")
                when (e) {
                    is UnknownHostException -> {
                        Log.d("TAG", "getBookedEventServices when called: $e")
                        viewModelScope.launch {
                            callBackExpListInterface?.internetError(
                                AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION,
                                caller
                            )
                        }
                    }
                    is ConnectException -> {
                        viewModelScope.launch {
                            callBackExpListInterface?.internetError(
                                AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION,
                                caller
                            )
                        }
                    }
                }
            }
        }

    // 2801 - Booked Event Services API For Modify Slots
    fun getModifyBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        isMonth: Boolean,
        fromDate: String,
        toDate: String, caller: String,
    ) =
        liveData(Dispatchers.IO) {
            try {
                emit(
                    scheduleManagementRepository.getModifyBookedEventServices(
                        idToken,
                        spRegId,
                        serviceCategoryId,
                        serviceVendorOnBoardingId,
                        isMonth,
                        fromDate,
                        toDate
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is UnknownHostException -> {
                        Log.d("TAG", "getBookedEventServices when mody called: $e")
                        viewModelScope.launch {
                            callBackExpListInterface?.internetError(
                                AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION,
                                caller
                            )
                        }
                    }
                    is ConnectException -> {
                        viewModelScope.launch {
                            callBackExpListInterface?.internetError(
                                AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION,
                                caller
                            )
                        }
                    }
                }
            }
        }

    private var callBackInterface: CallBackInterface? = null

    // 2458 Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // 2458 CallBack Interface
    interface CallBackInterface {
        fun itemClick(msg: Int)
        fun branchItemClick(
            serviceVendorOnboardingId: Int,
            name: String?,
            allServiceposition: Int?
        )

        fun internetError(exception: String)
    }

    private var callBackExpListInterface: CallBackExpListInterface? = null

    // 2458 Initializing CallBack Interface Method
    fun setCallBackExpListInterface(callback: CallBackExpListInterface) {
        callBackExpListInterface = callback
    }

    // 3061 CallBack Interface
    interface CallBackExpListInterface {
        fun internetError(exception: String, tag: String)
    }

}