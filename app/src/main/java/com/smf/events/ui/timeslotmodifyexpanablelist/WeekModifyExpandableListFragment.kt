package com.smf.events.ui.timeslotmodifyexpanablelist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.databinding.FragmentTimeSlotsExpandableListBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslot.deselectingdialog.DeselectingDialogFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.adapter.CustomModifyExpandableListAdapterDay
import com.smf.events.ui.timeslotmodifyexpanablelist.model.Data
import com.smf.events.ui.timeslotmodifyexpanablelist.model.ModifyBookedServiceEvents
import com.smf.events.ui.timeslotsexpandablelist.model.BookedEventServiceDto
import com.smf.events.ui.timeslotsexpandablelist.model.ListData
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WeekModifyExpandableListFragment : Fragment(),
    CustomModifyExpandableListAdapterDay.TimeSlotIconClickListener,
    Tokens.IdTokenCallBackInterface {

    private var TAG = "WeekModifyExpandableListFragment"
    private var expandableListView: ExpandableListView? = null
    private var adapter: CustomModifyExpandableListAdapterDay? = null
    private var childData = HashMap<String, List<ListData>>()
    private var titleDate = ArrayList<String>()
    private lateinit var mDataBinding: FragmentTimeSlotsExpandableListBinding
    var spRegId: Int = 0
    lateinit var idToken: String
    var roleId: Int = 0
    var serviceCategoryId: Int? = null
    var serviceVendorOnboardingId: Int? = null
    var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    private var fromDate: String? = null
    private var toDate: String? = null
    private var weekList: ArrayList<String> = ArrayList()
    var parent: ViewGroup? = null
    private var groupPosition: Int = 0
    private var weekMap: HashMap<Int, ArrayList<String>>? = null
    private var listOfDatesArray: ArrayList<ArrayList<String>> = ArrayList()
    var listOfDates: ArrayList<String>? = ArrayList()
    private lateinit var dialogDisposable: Disposable

    companion object {
        private var lastGroupPosition: Int = 0
    }

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var tokens: Tokens

    // SharedViewModel Initialization
    private val sharedViewModel: ScheduleManagementViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mDataBinding = FragmentTimeSlotsExpandableListBinding.inflate(inflater, container, false)
        return mDataBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2670 - Initialize expendableList
        expandableListView = mDataBinding.expendableList
        // 2670 - Initialize Local Variables
        setIdTokenAndSpRegId()
        // 2670 - Token Class CallBack Initialization
        tokens.setCallBackInterface(this)

        sharedViewModel.getCurrentWeekDate.observe(requireActivity(),
            { currentWeekDate ->
                serviceCategoryIdAndServiceVendorOnboardingId(currentWeekDate)
                weekMap = getWeekListMap(currentWeekDate)
                listOfDatesArray.clear()
                weekMap?.forEach { it ->
                    listOfDatesArray.add(it.value)
                }
                weekList = currentWeekDate.weekList
                listOfDates = currentWeekDate.bookedWeekList
                fromDate = weekList.first()
                toDate = weekList.last()
                initializeExpandableViewData()
            })

        // Observe Modify Dialog Result
        observeModifyDialogResult()
    }

    // 2815 - Method For Observe Result From ModifyDialog
    private fun observeModifyDialogResult() {
        dialogDisposable = RxBus.listen(RxEvent.ModifyDialog::class.java).subscribe {
            if (it.status == AppConstants.WEEK) {
                Log.d(TAG, "onViewCreated listener week: called")
                apiTokenValidation(AppConstants.AVAILABLE)
            }
        }
    }

    // 2776 - Method For set week wise Dates ArrayList
    private fun initializeExpandableViewData() {
        if (weekMap.isNullOrEmpty()) {
            mDataBinding.expendableList.visibility = View.GONE
            mDataBinding.noEventsText.visibility = View.VISIBLE
        } else {
            mDataBinding.expendableList.visibility = View.VISIBLE
            mDataBinding.noEventsText.visibility = View.GONE
            apiTokenValidation(AppConstants.INITIAL_WEEK)
        }
    }

    // 2670 - Method For Get Booked Event Services
    private fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String,
        caller: String,
    ) {
        sharedViewModel.getModifyBookedEventServices(
            idToken, spRegId, serviceCategoryId,
            serviceVendorOnBoardingId,
            false, fromDate, toDate
        ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
            when (apiResponse) {
                is ApisResponse.Success -> {
                    Log.d(TAG, "success ModifyBookedEvent weekaa: ${apiResponse.response.data}")
                    if (caller == AppConstants.INITIAL_WEEK) {
                        eventsOnSelectedDateApiValueUpdate(apiResponse, caller)
                    } else {
                        setDataToExpandableList(apiResponse, groupPosition)
                        adapter!!.notifyDataSetChanged()
                    }
                }
                is ApisResponse.Error -> {
                    Log.d(
                        TAG,
                        "check token result success ModifyBookedEvent exp: ${apiResponse.exception}"
                    )
                }
                else -> {
                }
            }
        })
    }

    // 2815 - Method For Set Null Value
    private fun nullListData(data: Data): ListData {
        return ListData(
            data.serviceSlot,
            listOf(BookedEventServiceDto(getString(R.string.null_text), "", "", ""))
        )
    }

    // 2815 - Method For Set available Value
    private fun isEmptyAvailableListData(data: Data): ListData {
        return ListData(
            data.serviceSlot,
            listOf(
                BookedEventServiceDto(
                    getString(R.string.available_small),
                    "",
                    "",
                    ""
                )
            )
        )
    }

    // 2795 - Method For Set Data To ExpandableList
    private fun setDataToExpandableList(
        apiResponse: ApisResponse.Success<ModifyBookedServiceEvents>,
        position: Int
    ) {
        Log.d(TAG, "setDataToExpandableList position: $position")
        val bookedEventDetails = ArrayList<ListData>()
        apiResponse.response.data.forEach { data ->
            if (data.bookedEventServiceDtos == null) {
                bookedEventDetails.add(nullListData(data))
            } else if (data.bookedEventServiceDtos.isEmpty()) {
                bookedEventDetails.add(isEmptyAvailableListData(data))
            } else {
                val bookedEventServiceDtos = updateUpcomingEvents(data)
                bookedEventDetails.add(
                    ListData(
                        data.serviceSlot,
                        bookedEventServiceDtos
                    )
                )
            }
        }
        Log.d(TAG, "setDataToExpandableList pos11: $bookedEventDetails")
        childData[titleDate[position]] = bookedEventDetails
    }

    // 2873 - Restrict Completed Dates
    private fun updateUpcomingEvents(data: Data): ArrayList<BookedEventServiceDto> {
        val bookedEventServiceDtos = ArrayList<BookedEventServiceDto>()
        data.bookedEventServiceDtos?.forEach { objectList ->
            val currentDayFormatter =
                DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
            val eventDate = LocalDate.parse(objectList.eventDate, currentDayFormatter)
            if (eventDate >= LocalDate.now()) {
                bookedEventServiceDtos.add(objectList)
            }
        }
        return bookedEventServiceDtos
    }

    private fun eventsOnSelectedDateApiValueUpdate(
        apiResponse: ApisResponse.Success<ModifyBookedServiceEvents>,
        caller: String
    ) {
        childData.clear()
        titleDate.clear()
        Log.d(TAG, "eventsOnSelectedDateApiValueUpdate value: ${listOfDatesArray}")
        for (i in 0 until listOfDatesArray.size) {
            Log.d(TAG, "eventsOnSelectedDateApiValueUpdate for: ${listOfDatesArray[i]}")
            val bookedEventDetails = ArrayList<ListData>()
            apiResponse.response.data.forEach {
                if (it.bookedEventServiceDtos == null) {
                    bookedEventDetails.add(nullListData(it))
                } else if (it.bookedEventServiceDtos.isEmpty()) {
                    bookedEventDetails.add(isEmptyAvailableListData(it))
                } else {
                    val bookedEventServiceDtos = updateUpcomingEvents(it)
                    bookedEventDetails.add(
                        ListData(
                            it.serviceSlot,
                            bookedEventServiceDtos
                        )
                    )
                }
            }
            titleDate.add(
                "${getMonth(listOfDatesArray[i][0])}  ${dateFormat(listOfDatesArray[i][0])} - ${
                    dateFormat(listOfDatesArray[i][listOfDatesArray[i].lastIndex])
                }"
            )
            childData[titleDate[i]] = bookedEventDetails
        }
        Log.d(TAG, "getBookedEventServices childData weekaaaa: $childData")
        initializeExpandableListSetUp(caller)
    }

    // 2558 - Method for ExpandableList Initialization
    private fun initializeExpandableListSetUp(caller: String) {
        if (expandableListView != null) {
            adapter = CustomModifyExpandableListAdapterDay(
                requireContext(),
                getString(R.string.week),
                titleDate,
                childData
            )
            expandableListView!!.setAdapter(adapter)
            adapter?.setOnClickListener(this)
        }
        // Condition For Expand selected Week TimeSlot
        if (caller == AppConstants.INITIAL_WEEK) {
            for (i in 0 until listOfDatesArray.size) {
                if (listOfDatesArray[i][0] == weekList[0]) {
                    Log.d(
                        TAG,
                        "initializeExpandableListSetUp: ${listOfDatesArray.indexOf(listOfDatesArray[i])}"
                    )
                    expandableListView?.expandGroup(listOfDatesArray.indexOf(listOfDatesArray[i]))
                    lastGroupPosition = listOfDatesArray.indexOf(listOfDatesArray[i])
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    // 2776 -  Method For Perform Group Click Events
    override fun onGroupClick(parent: ViewGroup, listPosition: Int, isExpanded: Boolean) {
        Log.d(TAG, "onGroupClick week: called ${listOfDatesArray[listPosition]}")
        this.parent = parent as ExpandableListView
        this.groupPosition = listPosition
        this.weekList = listOfDatesArray[listPosition]
        fromDate = listOfDatesArray[listPosition][0]
        toDate = listOfDatesArray[listPosition][listOfDatesArray[listPosition].lastIndex]
        Log.d(TAG, "onGroupClick week: $fromDate $toDate")
        if (isExpanded) {
            parent.collapseGroup(groupPosition)
        } else {
            // Send Selected Week To ViewModel For Calender UI Display
            sharedViewModel.setExpCurrentWeek(listOfDatesArray[listPosition])
            val bookedEventDetails = ArrayList<ListData>()
            bookedEventDetails.add(
                ListData(
                    getString(R.string.empty),
                    listOf(BookedEventServiceDto("", "", "", ""))
                )
            )
            childData[titleDate[groupPosition]] = bookedEventDetails
            parent.collapseGroup(lastGroupPosition)
            parent.expandGroup(groupPosition)
            apiTokenValidation(AppConstants.BOOKED_EVENTS_SERVICES_FROM_SELECTED_WEEK)
        }
        lastGroupPosition = listPosition
    }

    override fun onChildClick(listPosition: Int, expandedListPosition: Int, timeSlot: String) {
        val branchName =
            childData[titleDate[listPosition]]?.get(expandedListPosition)?.status?.get(0)?.branchName
        val currentDayFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
        val currentMonth = LocalDate.parse(fromDate, currentDayFormatter).month.getDisplayName(
            TextStyle.FULL,
            Locale.ENGLISH
        )
        val statusList = childData[titleDate[listPosition]]?.get(expandedListPosition)?.status
        Log.d(TAG, "onChildClick week: called $branchName")
        when (branchName) {
            getString(R.string.available_small) -> {
                serviceVendorOnboardingId?.let { serviceVendorOnboardingId ->
                    fromDate?.let { fromDate ->
                        toDate?.let { toDate ->
                            DeselectingDialogFragment.newInstance(
                                AppConstants.WEEK,
                                AppConstants.DESELECTED,
                                timeSlot,
                                currentMonth,
                                serviceVendorOnboardingId,
                                fromDate,
                                toDate, statusList
                            )
                                .show(
                                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                                    DeselectingDialogFragment.TAG
                                )
                        }
                    }
                }
            }
            getString(R.string.null_text) -> {
                fromDate?.let { fromDate ->
                    serviceVendorOnboardingId?.let { serviceVendorOnboardingId ->
                        toDate?.let { toDate ->
                            modifyNullApiUpdate(
                                fromDate,
                                true,
                                timeSlot,
                                serviceVendorOnboardingId,
                                toDate
                            )
                        }
                    }
                }
            }
            else -> {
                serviceVendorOnboardingId?.let { serviceVendorOnboardingId ->
                    fromDate?.let { fromDate ->
                        toDate?.let { toDate ->
                            DeselectingDialogFragment.newInstance(
                                AppConstants.WEEK,
                                AppConstants.SELECTED,
                                timeSlot,
                                currentMonth,
                                serviceVendorOnboardingId,
                                fromDate,
                                toDate,
                                statusList
                            )
                                .show(
                                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                                    DeselectingDialogFragment.TAG
                                )
                        }
                    }
                }
            }
        }

        // Setting Position From Selected Calender Date
        this.groupPosition = listPosition
        lastGroupPosition = listPosition
    }

    // 2815 - Method For getModifyWeekSlot Api call with NULL
    private fun modifyNullApiUpdate(
        fromDate: String,
        isAvailable: Boolean,
        timeSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String
    ) {
        sharedViewModel.getModifyWeekSlot(
            idToken, spRegId, fromDate, isAvailable, timeSlot,
            serviceVendorOnBoardingId,
            toDate
        ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
            when (apiResponse) {
                is ApisResponse.Success -> {
                    Log.d(TAG, "success ModifyBookedEvent null: ${apiResponse.response.data}")
                    apiTokenValidation(AppConstants.NULL)
                }
                is ApisResponse.Error -> {
                    Log.d(TAG, "success ModifyBookedEvent null error: ${apiResponse.exception}")
                }
                else -> {
                    Log.d(TAG, "Condition Not Satisfied")
                }
            }
        })
    }

    // 2670 - Method For AWS Token Validation
    private fun apiTokenValidation(caller: String) {
        if (idToken.isNotEmpty()) {
            tokens.checkTokenExpiry(
                requireActivity().applicationContext as SMFApp,
                caller, idToken
            )
        }
    }

    // 2670 - Callback From Token Class
    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Dispatchers.Main) {
            Log.d(TAG, "tokenCallBack week: $fromDate $toDate")
            fromDate?.let { fromDate ->
                toDate?.let { toDate ->
                    getBookedEventServices(
                        idToken, spRegId,
                        serviceCategoryId, serviceVendorOnboardingId,
                        fromDate, toDate, caller
                    )
                }
            }
        }
    }

    // 2670 - Method For Date And Day Arrangement To Display UI
    private fun dateFormat(input: String): String {
        val date = input.substring(3, 5)
        val currentDayFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
        val currentDay = LocalDate.parse(input, currentDayFormatter).dayOfWeek.getDisplayName(
            TextStyle.SHORT, Locale.ENGLISH
        )
        return "$date $currentDay"
    }

    // 2670 - Method For Month Arrangement To Display UI
    private fun getMonth(input: String): String {
        var monthCount = input.substring(0, 2)
        if (monthCount[0].digitToInt() == 0) {
            monthCount = monthCount[1].toString()
        }
        val month = Month.of(monthCount.toInt()).toString().substring(0, 3).let { month ->
            month.substring(0, 1) + month.substring(1, 2)
                .lowercase(Locale.getDefault()) + month.substring(2, 3)
                .lowercase(Locale.getDefault())
        }
        return month
    }

    // 2691 - method For Set Local Variable Value serviceCategoryId And ServiceVendorOnBoardingId
    private fun serviceCategoryIdAndServiceVendorOnboardingId(currentWeekDate: ScheduleManagementViewModel.WeekDates) {
        when {
            currentWeekDate.seviceId == 0 -> {
                serviceCategoryId = null
                serviceVendorOnboardingId = null
            }
            currentWeekDate.branchId == 0 -> {
                serviceCategoryId = currentWeekDate.seviceId
                serviceVendorOnboardingId = null
            }
            else -> {
                serviceCategoryId = currentWeekDate.seviceId
                serviceVendorOnboardingId = currentWeekDate.branchId
            }
        }
    }

    // 2776 - method For getting Week Map List
    private fun getWeekListMap(currentWeekDate: ScheduleManagementViewModel.WeekDates): HashMap<Int, ArrayList<String>> {
        val weekMap = HashMap<Int, ArrayList<String>>()
        currentWeekDate.weekListMapOfMonth.forEach {
            Log.d(TAG, "getWeekListMap: ${it.key}")
            val weekFromDate = currentWeekDate.weekListMapOfMonth[it.key]?.fromDate
            val weekList = ArrayList<String>()
            for (a in 0 until 7) {
                if (weekFromDate != null) {
                    weekList.add(weekFromDate.plusDays(a.toLong()).format(dateFormatter))
                }
            }
            weekMap[it.key] = weekList
        }
        return weekMap
    }

    // 2670 - Method For Set IdToken And SpRegId From SharedPreferences
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        roleId = sharedPreference.getInt(SharedPreference.ROLE_ID)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }

}