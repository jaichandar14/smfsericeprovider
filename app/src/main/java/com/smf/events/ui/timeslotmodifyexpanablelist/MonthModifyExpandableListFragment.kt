package com.smf.events.ui.timeslotmodifyexpanablelist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.databinding.FragmentTimeSlotsExpandableListBinding
import com.smf.events.helper.*
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.schedulemanagement.ScheduleManagementActivity
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

class MonthModifyExpandableListFragment : Fragment(),
    CustomModifyExpandableListAdapter.TimeSlotIconClickListener, Tokens.IdTokenCallBackInterface {

    private var TAG = this::class.java.name
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
    private val currentDayFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
    private var fromDate: String? = null
    private var toDate: String? = null
    private var currentDate: String? = null
    private var monthValue: String = ""
    private var groupPosition: Int = 0
    private lateinit var dialogDisposable: Disposable

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
        // 2558 - getDate ScheduleManagementViewModel Observer
        sharedViewModel.getCurrentMonthDate.observe(
            viewLifecycleOwner
        ) { currentMonthDate ->
            //  2986 Showing progress based on calender and service selection
            mDataBinding.modifyProgressBar.visibility = View.VISIBLE
            mDataBinding.expandableLayout.visibility = View.GONE
            fromDate = currentMonthDate.fromDate
            toDate = currentMonthDate.toDate
            currentDate = currentMonthDate.currentDate
            monthValue = currentMonthDate.monthValue.toString()
            CalendarUtils.allDaysListForMonth = currentMonthDate.monthFromAndToDate
            serviceCategoryIdAndServiceVendorOnboardingId(currentMonthDate)
            monthValidation()
        }

        // Observe Modify Dialog Result
        observeModifyDialogResult()

    }

    // 2823 - Method For Observe Result From ModifyDialog
    private fun observeModifyDialogResult() {
        dialogDisposable = RxBus.listen(RxEvent.ModifyDialog::class.java).subscribe {
            showProgress()
            if (it.status == AppConstants.MONTH) {
                Log.d(TAG, "onViewCreated listener month: called")
                apiTokenValidation(AppConstants.AVAILABLE)
            }
        }
    }

    // 2795 - Method For Restrict Previous Month
    fun monthValidation() {
        if (!fromDate.isNullOrEmpty() && !toDate.isNullOrEmpty()) {
            mDataBinding.expendableList.visibility = View.VISIBLE
            mDataBinding.noEventsText.visibility = View.GONE
            init()
        } else {
            mDataBinding.modifyProgressBar.visibility = View.GONE
            mDataBinding.expendableList.visibility = View.GONE
            mDataBinding.noEventsText.visibility = View.VISIBLE
        }
    }

    private fun init() {
        // 2670 - Api Call Token Validation
        apiTokenValidation(AppConstants.BOOKED_EVENTS_SERVICES_INITIAL)
    }

    // 2670 - Method For Get Booked Event Services
    fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String,
        caller: String
    ) {
        view?.let {
            sharedViewModel.getModifyBookedEventServices(
                idToken, spRegId, serviceCategoryId,
                serviceVendorOnBoardingId, true,
                fromDate,
                toDate, AppConstants.MONTH
            ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        //  2986 Hiding progress based on calender and service selection
                        mDataBinding.modifyProgressBar.visibility = View.GONE
                        mDataBinding.expandableLayout.visibility = View.VISIBLE
                        Log.d(TAG, "success month mody: ${apiResponse.response.data}")
                        if (caller == AppConstants.BOOKED_EVENTS_SERVICES_INITIAL) {
                            eventsOnSelectedDateApiValueUpdate(apiResponse, caller)
                        } else {
                            setDataToExpandableList(apiResponse, groupPosition)
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                    is ApisResponse.CustomError -> {
                        Log.d("TAG", "check token result: ${apiResponse.message}")
                        sharedViewModel.setToastMessageG(
                            apiResponse.message,
                            Snackbar.LENGTH_LONG,
                            AppConstants.PLAIN_SNACK_BAR
                        )
                        mDataBinding.modifyProgressBar.visibility = View.GONE
                        mDataBinding.expandableLayout.visibility = View.VISIBLE}
                    is ApisResponse.InternetError -> {
                        (requireActivity() as ScheduleManagementActivity).showInternetDialog(
                            apiResponse.message
                        )
                    }
                    else -> {}
                }
            })
        }
    }

    // 2795 - Method For Set Data To ExpandableList
    private fun setDataToExpandableList(
        apiResponse: ApisResponse.Success<ModifyBookedServiceEvents>,
        position: Int
    ) {
        Log.d(TAG, "setDataToExpandableList position: $position")
        val bookedEventDetails = ArrayList<ListDataModify>().apply {
            apiResponse.response.data.forEach { data ->
                if (data.bookedEventServiceDtos == null) {
                   this.add(nullListData(data))
                } else if (data.bookedEventServiceDtos.isEmpty()) {
                    this.add(isEmptyAvailableListData(data))
                } else {
                    val bookedEventServiceDtos = updateUpcomingEvents(data)
                    this.add(
                        ListDataModify(
                            data.serviceSlot,
                            bookedEventServiceDtos
                        )
                    )
                }
            }
        }
        Log.d(TAG, "setDataToExpandableList pos11: $bookedEventDetails")
        childData[titleDate[position]] = bookedEventDetails
    }

    // 2873 - Restrict Completed Dates
    fun updateUpcomingEvents(data: Data): ArrayList<BookedEventServiceDtoModify> {
        val bookedEventServiceDtos = ArrayList<BookedEventServiceDtoModify>().apply {
            data.bookedEventServiceDtos?.forEach { objectList ->
                val currentDayFormatter =
                    DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT, Locale.ENGLISH)
                val eventDate = LocalDate.parse(objectList.eventDate, currentDayFormatter)
                if (eventDate >= LocalDate.now()) {
                    this.add(objectList)
                }
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
        addTitle()
        val bookedEventDetails = ArrayList<ListDataModify>().apply {
            apiResponse.response.data.forEach {
                if (it.bookedEventServiceDtos == null) {
                    this.add(nullListData(it))
                } else if (it.bookedEventServiceDtos.isEmpty()) {
                    this.add(isEmptyAvailableListData(it))
                } else {
                    val bookedEventServiceDtos = updateUpcomingEvents(it)
                    this.add(
                        ListDataModify(
                            it.serviceSlot,
                            bookedEventServiceDtos
                        )
                    )
                }
            }
        }
        childData[titleDate[0]] = bookedEventDetails
        Log.d(TAG, "getBookedEventServices childData week: $childData")
        initializeExpandableListSetUp()
    }

    // 2558 - Method for ExpandableList Initialization
    private fun initializeExpandableListSetUp() {
        if (expandableListView != null) {
            adapter = CustomModifyExpandableListAdapter(
                requireContext(),
                getString(R.string.month),
                titleDate,
                childData
            )
            expandableListView!!.setAdapter(adapter)
            adapter?.setOnClickListener(this)

            val businessExpDate =
                CalendarUtils.businessValidity?.format(CalendarUtils.dateFormatter)
            val businessValidationDateLocalDate =
                LocalDate.parse(businessExpDate, CalendarUtils.dateFormatter)
            if (CalendarUtils.allDaysListForMonth.contains(businessExpDate)) {
                // Default Expansion Into The ExpandableList
                expandableListView!!.expandGroup(groupPosition)
                adapter!!.notifyDataSetChanged()
            } else {
                CalendarUtils.allDaysListForMonth.forEach {
                    val currentDay = LocalDate.parse(it, CalendarUtils.dateFormatter)
                    if (currentDay < businessValidationDateLocalDate) {
                        expandableListView!!.expandGroup(groupPosition)
                        adapter!!.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onChildClick(listPosition: Int, expandedListPosition: Int, timeSlot: String) {
        val businessExpDate =
            CalendarUtils.businessValidity?.plusDays(1)?.format(CalendarUtils.dateFormatter)
        if (CalendarUtils.allDaysListForMonth.contains(businessExpDate)) {
            DeselectingDialogFragment.newInstance(
                AppConstants.MONTH,
                AppConstants.EXPMonth,
                AppConstants.TIMESLOT,
                AppConstants.BID_SUBMITTED,
                0,
                CalendarUtils.businessValidity?.format(CalendarUtils.dateFormatter).toString(),
                AppConstants.BID_REJECTED, null
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
                                    AppConstants.MONTH,
                                    AppConstants.DESELECTED,
                                    timeSlot,
                                    currentMonth,
                                    serviceVendorOnboardingId,
                                    fromDate,
                                    toDate, onlyBookedList
                                ).show(
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
                                    AppConstants.MONTH,
                                    AppConstants.NULL_TO_SELECT,
                                    timeSlot,
                                    currentMonth,
                                    serviceVendorOnboardingId,
                                    fromDate,
                                    toDate, onlyBookedList
                                ).show(
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
                                    AppConstants.MONTH,
                                    AppConstants.SELECTED,
                                    timeSlot,
                                    currentMonth,
                                    serviceVendorOnboardingId,
                                    fromDate,
                                    toDate,
                                    onlyBookedList
                                ).show(
                                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                                    DeselectingDialogFragment.TAG
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onGroupClick(
        parent: ViewGroup,
        listPosition: Int,
        isExpanded: Boolean,
        businessValidationStatus: Boolean
    ) {
        if (businessValidationStatus) {
//            Toast.makeText(
//                requireContext(),
//                "Your Business registration valid to date is No longer available for the selected date",
//                Toast.LENGTH_LONG
//            ).show()
            sharedViewModel.setToastMessageG(
                "Your Business registration valid to date is No longer available for the selected date",
                Snackbar.LENGTH_LONG,
                AppConstants.PLAIN_SNACK_BAR
            )
        }
    }

    // 2697 - Method For Add ExpandableList Title Group
    private fun addTitle() {
        titleDate.add(
            "${fromDate?.let { getMonth(it) }}  ${fromDate?.let { dateFormat(it) }} - ${
                toDate?.let {
                    dateFormat(
                        it
                    )
                }
            }"
        )
    }

    // 2815 - Method For Set Null Value
    private fun nullListData(data: Data): ListDataModify {
        return ListDataModify(
            data.serviceSlot,
            listOf(BookedEventServiceDtoModify(getString(R.string.null_text), "", "", "", ""))
        )
    }

    // 2815 - Method For Set available Value
    fun isEmptyAvailableListData(data: Data): ListDataModify {
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

    // 2815 - Method For Set Empty Value
    private fun emptyListData(): ListDataModify {
        return ListDataModify(
            "",
            listOf(BookedEventServiceDtoModify("", "", "", "", ""))
        )
    }

    // 2691 - method For Set Local Variable Value serviceCategoryId And ServiceVendorOnBoardingId
    private fun serviceCategoryIdAndServiceVendorOnboardingId(currentMonthDate: ScheduleManagementViewModel.MonthDates) {
        when {
            currentMonthDate.seviceId == 0 -> {
                serviceCategoryId = null
                serviceVendorOnboardingId = null
            }
            currentMonthDate.branchId == 0 -> {
                serviceCategoryId = currentMonthDate.seviceId
                serviceVendorOnboardingId = null
            }
            else -> {
                serviceCategoryId = currentMonthDate.seviceId
                serviceVendorOnboardingId = currentMonthDate.branchId
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
        view?.let {
            withContext(Dispatchers.Main) {
                Log.d(TAG, "tokenCallBack month: $fromDate")
                fromDate?.let { currentDate ->
                    toDate?.let { toDate ->
                        getBookedEventServices(
                            idToken, spRegId,
                            serviceCategoryId, serviceVendorOnboardingId,
                            currentDate, toDate, caller
                        )
                    }
                }
            }
        }
    }

    // 2670 - Method For Set IdToken And SpRegId From SharedPreferences
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        roleId = sharedPreference.getInt(SharedPreference.ROLE_ID)
    }

    // 2670 - Method For Date And Day Arrangement To Display UI
    private fun dateFormat(input: String): String {
        val date = input.substring(3, 5)
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

    // 2952 - Visible Progress bar during Modify Availability
    private fun showProgress() {
        val bookedEventDetails = ArrayList<ListDataModify>().apply {
            this.add(
                ListDataModify(
                    getString(R.string.empty),
                    listOf(BookedEventServiceDtoModify("", "", "", "", ""))
                )
            )
        }
        childData[titleDate[groupPosition]] = bookedEventDetails
        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onViewCreated: observe onDestroy:moth mody")
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }

}