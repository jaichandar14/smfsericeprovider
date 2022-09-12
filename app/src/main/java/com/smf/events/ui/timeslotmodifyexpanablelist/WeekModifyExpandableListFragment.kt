package com.smf.events.ui.timeslotmodifyexpanablelist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.databinding.FragmentTimeSlotsExpandableListBinding
import com.smf.events.helper.*
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslot.deselectingdialog.DeselectingDialogFragment
import com.smf.events.ui.timeslotmodifyexpanablelist.adapter.CustomModifyExpandableListAdapter
import com.smf.events.ui.timeslotmodifyexpanablelist.model.BookedEventServiceDtoModify
import com.smf.events.ui.timeslotmodifyexpanablelist.model.Data
import com.smf.events.ui.timeslotmodifyexpanablelist.model.ModifyBookedServiceEvents
import com.smf.events.ui.timeslotsexpandablelist.model.ListDataModify
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
import kotlin.concurrent.schedule

class WeekModifyExpandableListFragment : Fragment(),
    CustomModifyExpandableListAdapter.TimeSlotIconClickListener,
    Tokens.IdTokenCallBackInterface, ScheduleManagementViewModel.CallBackExpListInterface {

    private var TAG = "WeekModifyExpandableListFragment"
    private var expandableListView: ExpandableListView? = null
    private var adapter: CustomModifyExpandableListAdapter? = null
    private var childData = HashMap<String, List<ListDataModify>>()
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
    private var isScroll: Boolean = false
    private lateinit var internetErrorDialog: InternetErrorDialog

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
        // 2458 Scheduled Management  ViewModel CallBackInterface
        sharedViewModel.setCallBackExpListInterface(this)
        // Internet Error Dialog Initialization
        internetErrorDialog = InternetErrorDialog.newInstance()

        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer weekMody")
            internetErrorDialog.dismissDialog()
            if (activity != null) {
                init()
            }
        }

        sharedViewModel.getCurrentWeekDate.observe(viewLifecycleOwner,
            { currentWeekDate ->
                //  2986 Showing progress based on calender and service selection
                mDataBinding.modifyProgressBar.visibility = View.VISIBLE
                mDataBinding.expandableLayout.visibility = View.GONE
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
                isScroll = currentWeekDate.isScroll
                CalendarUtils.listOfDatesArray = listOfDatesArray
                Log.d(TAG, "onViewCreated week: ${currentWeekDate.isScroll}, $listOfDatesArray")
                initializeExpandableViewData()
            })

        // Observe Modify Dialog Result
        observeModifyDialogResult()
    }

    // 2815 - Method For Observe Result From ModifyDialog
    private fun observeModifyDialogResult() {
        dialogDisposable = RxBus.listen(RxEvent.ModifyDialog::class.java).subscribe {
            showProgress()
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
            mDataBinding.modifyProgressBar.visibility = View.GONE
            mDataBinding.noEventsText.visibility = View.VISIBLE
        } else {
            mDataBinding.expendableList.visibility = View.VISIBLE
            mDataBinding.noEventsText.visibility = View.GONE
            if (internetErrorDialog.checkInternetAvailable(requireContext())) {
                init()
            }
        }
    }

    private fun init() {
        apiTokenValidation(AppConstants.INITIAL_WEEK)
    }

    // 2670 - Method For Get Booked Event Services
    private fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String,
        caller: String,
    ) {
        if (view != null) {
            sharedViewModel.getModifyBookedEventServices(
                idToken, spRegId, serviceCategoryId,
                serviceVendorOnBoardingId,
                false, fromDate, toDate, AppConstants.WEEK
            ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        //  2986 Hiding progress based on calender and service selection
                        mDataBinding.modifyProgressBar.visibility = View.GONE
                        mDataBinding.expandableLayout.visibility = View.VISIBLE
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
    }

    // 2815 - Method For Set Null Value
    private fun nullListData(data: Data): ListDataModify {
        return ListDataModify(
            data.serviceSlot,
            listOf(BookedEventServiceDtoModify(getString(R.string.null_text), "", "", "", ""))
        )
    }

    // 2815 - Method For Set available Value
    private fun isEmptyAvailableListData(data: Data): ListDataModify {
        return ListDataModify(
            data.serviceSlot,
            listOf(
                BookedEventServiceDtoModify(
                    getString(R.string.available_small),
                    "",
                    "",
                    "", ""
                )
            )
        )
    }

    // 2795 - Method For Set Data To ExpandableList
    private fun setDataToExpandableList(
        apiResponse: ApisResponse.Success<ModifyBookedServiceEvents>,
        position: Int,
    ) {
        Log.d(TAG, "setDataToExpandableList position: $position")
        val bookedEventDetails = ArrayList<ListDataModify>()
        apiResponse.response.data.forEach { data ->
            if (data.bookedEventServiceDtos == null) {
                bookedEventDetails.add(nullListData(data))
            } else if (data.bookedEventServiceDtos.isEmpty()) {
                bookedEventDetails.add(isEmptyAvailableListData(data))
            } else {
                val bookedEventServiceDtos = updateUpcomingEvents(data)
                bookedEventDetails.add(
                    ListDataModify(
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
    private fun updateUpcomingEvents(data: Data): ArrayList<BookedEventServiceDtoModify> {
        val bookedEventServiceDtos = ArrayList<BookedEventServiceDtoModify>()
        data.bookedEventServiceDtos?.forEach { objectList ->
            val currentDayFormatter =
                DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT, Locale.ENGLISH)
            val eventDate = LocalDate.parse(objectList.eventDate, currentDayFormatter)
            if (eventDate >= LocalDate.now()) {
                bookedEventServiceDtos.add(objectList)
            }
        }
        return bookedEventServiceDtos
    }

    private fun eventsOnSelectedDateApiValueUpdate(
        apiResponse: ApisResponse.Success<ModifyBookedServiceEvents>,
        caller: String,
    ) {
        childData.clear()
        titleDate.clear()
        Log.d(TAG, "eventsOnSelectedDateApiValueUpdate value: ${listOfDatesArray}")
        for (i in 0 until listOfDatesArray.size) {
            Log.d(TAG, "eventsOnSelectedDateApiValueUpdate for: ${listOfDatesArray[i]}")
            val bookedEventDetails = ArrayList<ListDataModify>()
            apiResponse.response.data.forEach {
                if (it.bookedEventServiceDtos == null) {
                    bookedEventDetails.add(nullListData(it))
                } else if (it.bookedEventServiceDtos.isEmpty()) {
                    bookedEventDetails.add(isEmptyAvailableListData(it))
                } else {
                    val bookedEventServiceDtos = updateUpcomingEvents(it)
                    bookedEventDetails.add(
                        ListDataModify(
                            it.serviceSlot,
                            bookedEventServiceDtos
                        )
                    )
                }
            }
            Log.d(TAG, "eventsOnSelectedDateApiValueUpdate bookedList: $bookedEventDetails")
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
            adapter = CustomModifyExpandableListAdapter(
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
                listOfDatesArray[i].forEach {
                    val currentDay = LocalDate.parse(it, CalendarUtils.dateFormatter)
                    val businessValidationDate = CalendarUtils.businessValidity
                    if (currentDay <= businessValidationDate) {
                        if (listOfDatesArray[i][0] == weekList[0]) {
                            expandableListView?.expandGroup(
                                listOfDatesArray.indexOf(
                                    listOfDatesArray[i]
                                )
                            )
                            lastGroupPosition = listOfDatesArray.indexOf(listOfDatesArray[i])
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
        if (isScroll) {
            // Condition for scroll to specific time slot location
            Timer().schedule(500) {
                scrollToLocation()
            }
        }
    }

    private fun scrollToLocation() {
        val position = listOfDatesArray.indexOf(weekList)
        Log.d(TAG, "expandableList full height: ${expandableListView?.height}")
        Log.d(
            TAG,
            "expandableList selected header height : ${position * expandableListView?.get(position)?.height!!}"
        )
        sharedViewModel.setScrollViewToPosition(position * expandableListView?.get(position)?.height!!)
    }

    // 2776 -  Method For Perform Group Click Events
    override fun onGroupClick(
        parent: ViewGroup,
        listPosition: Int,
        isExpanded: Boolean,
        businessValidationStatus: Boolean,
    ) {
        if (internetErrorDialog.checkInternetAvailable(requireContext())) {
            val businessExpDateLocal = CalendarUtils.businessValidity?.plusDays(1)
            val businessExpDate =
                CalendarUtils.businessValidity?.plusDays(1)?.format(CalendarUtils.dateFormatter)
            listOfDatesArray[listPosition].forEach {
                val currentDay = LocalDate.parse(it, CalendarUtils.dateFormatter)
                if (currentDay < businessExpDateLocal) {
                    Log.d(TAG, "onGroupClick: if")
                    if (!businessValidationStatus || listOfDatesArray[listPosition].contains(
                            businessExpDate
                        )
                    ) {
                        Log.d(TAG, "onGroupClick week: called ${listOfDatesArray[listPosition]}")
                        this.parent = parent as ExpandableListView
                        this.groupPosition = listPosition
                        this.weekList = listOfDatesArray[listPosition]
                        fromDate = listOfDatesArray[listPosition][0]
                        toDate =
                            listOfDatesArray[listPosition][listOfDatesArray[listPosition].lastIndex]
                        Log.d(TAG, "onGroupClick week: $fromDate $toDate")
                        if (isExpanded) {
                            parent.collapseGroup(groupPosition)
                        } else {
                            // Send Selected Week To ViewModel For Calender UI Display
                            sharedViewModel.setExpCurrentWeek(listOfDatesArray[listPosition])
                            val bookedEventDetails = ArrayList<ListDataModify>()
                            bookedEventDetails.add(
                                ListDataModify(
                                    getString(R.string.empty),
                                    listOf(BookedEventServiceDtoModify("", "", "", "", ""))
                                )
                            )
                            childData[titleDate[groupPosition]] = bookedEventDetails
                            parent.collapseGroup(lastGroupPosition)
                            parent.expandGroup(groupPosition)
                            apiTokenValidation(AppConstants.BOOKED_EVENTS_SERVICES_FROM_SELECTED_WEEK)
                        }
                        lastGroupPosition = listPosition
                    }
//                else {
//                    Toast.makeText(requireContext(), "Business validation date expired", Toast.LENGTH_SHORT)
//                        .show()
//                }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Business validation date expired",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return
                }
            }
        }
    }

    override fun onChildClick(listPosition: Int, expandedListPosition: Int, timeSlot: String) {
        if (internetErrorDialog.checkInternetAvailable(requireContext())) {
            val businessExpDate =
                CalendarUtils.businessValidity?.plusDays(1)?.format(CalendarUtils.dateFormatter)
            if (listOfDatesArray[listPosition].contains(businessExpDate)) {
                DeselectingDialogFragment.newInstance(
                    AppConstants.MONTH,
                    AppConstants.EXPWeek,
                    AppConstants.TIMESLOT,
                    AppConstants.BID_SUBMITTED,
                    0,
                    businessExpDate.toString(),
                    AppConstants.BID_REJECTED, null, internetErrorDialog
                )
                    .show(
                        (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                        DeselectingDialogFragment.TAG
                    )
            } else {
                val branchName =
                    childData[titleDate[listPosition]]?.get(expandedListPosition)?.status?.get(0)?.branchName
                val currentDayFormatter =
                    DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT, Locale.ENGLISH)
                val currentMonth =
                    LocalDate.parse(fromDate, currentDayFormatter).month.getDisplayName(
                        TextStyle.FULL,
                        Locale.ENGLISH
                    )
                val statusList =
                    childData[titleDate[listPosition]]?.get(expandedListPosition)?.status
                val onlyBookedList = ArrayList<BookedEventServiceDtoModify>()
                statusList?.forEach {
                    if (it.bidStatus == AppConstants.WON_BID) {
                        onlyBookedList.add(it)
                    }
                }
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
                                        toDate, onlyBookedList,
                                        internetErrorDialog
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
                                    DeselectingDialogFragment.newInstance(
                                        AppConstants.WEEK,
                                        AppConstants.NULL_TO_SELECT,
                                        timeSlot,
                                        currentMonth,
                                        serviceVendorOnboardingId,
                                        fromDate,
                                        toDate, onlyBookedList, internetErrorDialog
                                    )
                                        .show(
                                            (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                                            DeselectingDialogFragment.TAG
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
                                        onlyBookedList, internetErrorDialog
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
        }
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
        val currentDayFormatter =
            DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT, Locale.ENGLISH)
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

    // 2952 - Visible Progress bar during Modify Availability
    private fun showProgress() {
        val bookedEventDetails = ArrayList<ListDataModify>()
        bookedEventDetails.add(
            ListDataModify(
                getString(R.string.empty),
                listOf(BookedEventServiceDtoModify("", "", "", "", ""))
            )
        )
        childData[titleDate[groupPosition]] = bookedEventDetails
        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onViewCreated: observe onDestroy: weekmody")
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }

    override fun internetError(exception: String, tag: String) {
        Log.d(TAG, "internetError: called week")
        SharedPreference.isInternetConnected = false
        if (internetErrorDialog.noInternetDialog?.isShowing != true) {
            internetErrorDialog.checkInternetAvailable(requireContext())
        }
    }

}