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

class DayModifyExpandableListFragment : Fragment(),
    CustomModifyExpandableListAdapterDay.TimeSlotIconClickListener,
    Tokens.IdTokenCallBackInterface {

    var TAG = "DayModifyExpandableListFragment"
    private var expandableListView: ExpandableListView? = null
    private var adapter: CustomModifyExpandableListAdapterDay? = null
    private var childData = HashMap<String, List<ListData>>()
    private var titleDate = ArrayList<String>()
    private lateinit var mDataBinding: FragmentTimeSlotsExpandableListBinding
    var spRegId: Int = 0
    lateinit var idToken: String
    private var roleId: Int = 0
    var serviceCategoryId: Int? = null
    var serviceVendorOnboardingId: Int? = null
    private var fromDate: String? = null
    private var toDate: String? = null
    var parent: ViewGroup? = null
    private var listOfDates: ArrayList<String>? = ArrayList()
    private var allDaysList: ArrayList<String> = ArrayList()
    private var groupPosition: Int = 0
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
        savedInstanceState: Bundle?
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
        sharedViewModel.getCurrentDate.observe(requireActivity(), { currentDate ->
            serviceCategoryIdAndServiceVendorOnboardingId(currentDate)
            fromDate = currentDate.selectedDate
            toDate = currentDate.selectedDate
            listOfDates = currentDate.listOfDays
            allDaysList = currentDate.allDaysList
            Log.d(TAG, "onViewCreated day: ${currentDate.allDaysList} $serviceVendorOnboardingId")
            initializeExpandableViewData()
        })
        // Observe Modify Dialog Result
        observeModifyDialogResult()
    }

    // 2814 - Method For Observe Result From ModifyDialog
    private fun observeModifyDialogResult() {
        dialogDisposable = RxBus.listen(RxEvent.ModifyDialog::class.java).subscribe {
            if (it.status == AppConstants.DAY) {
                Log.d(TAG, "onViewCreated listener day: called")
                apiTokenValidation(AppConstants.AVAILABLE)
            }
        }
    }

    // 2776 - Method For set Dates ArrayList
    private fun initializeExpandableViewData() {
        if (allDaysList.isNullOrEmpty()) {
            mDataBinding.expendableList.visibility = View.GONE
            mDataBinding.noEventsText.visibility = View.VISIBLE
        } else {
            mDataBinding.expendableList.visibility = View.VISIBLE
            mDataBinding.noEventsText.visibility = View.GONE
            apiTokenValidation(AppConstants.INITIAL_DAY)
        }
    }

    // 2670 - Method For Get Booked Event Services
    private fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String,
        caller: String
    ) {
        Log.d(TAG, "success ModifyBookedEvent day : $fromDate $toDate ")
        sharedViewModel.getModifyBookedEventServices(
            idToken, spRegId, serviceCategoryId,
            serviceVendorOnBoardingId,
            false, fromDate, toDate
        ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
            when (apiResponse) {
                is ApisResponse.Success -> {
                    Log.d(TAG, "success ModifyBookedEvent day: ${apiResponse.response.data}")
                    if (caller == AppConstants.INITIAL_DAY) {
                        Log.d(TAG, "success ModifyBookedEvent day in: ${apiResponse.response.data}")
                        eventsOnSelectedDateApiValueUpdate(apiResponse, caller)
                    } else {
                        Log.d(
                            TAG,
                            "success ModifyBookedEvent day els: ${apiResponse.response.data}"
                        )
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

    private fun eventsOnSelectedDateApiValueUpdate(
        apiResponse: ApisResponse.Success<ModifyBookedServiceEvents>,
        caller: String
    ) {
        childData.clear()
        titleDate.clear()
        for (i in 0 until allDaysList.size) {
            val bookedEventDetails = ArrayList<ListData>()
            apiResponse.response.data.forEach {
                if (it.bookedEventServiceDtos == null) {
                    bookedEventDetails.add(nullListData(it))
                } else if (it.bookedEventServiceDtos.isEmpty()) {
                    bookedEventDetails.add(isEmptyAvailableListData(it))
                } else {
                    bookedEventDetails.add(
                        ListData(
                            it.serviceSlot,
                            it.bookedEventServiceDtos
                        )
                    )
                }
            }
            allDaysList[i].let { it -> dateFormat(it).let { titleDate.add(it) } }
            childData[titleDate[i]] = bookedEventDetails
        }
        Log.d(TAG, "getBookedEventServices childData day: $childData")
        initializeExpandableListSetUp(caller)
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
                bookedEventDetails.add(
                    ListData(
                        data.serviceSlot,
                        data.bookedEventServiceDtos
                    )
                )
            }
        }
        Log.d(TAG, "setDataToExpandableList pos11: $bookedEventDetails")
        childData[titleDate[position]] = bookedEventDetails
        Log.d(TAG, "setDataToExpandableList pos11: ${childData[titleDate[position]]}")
    }

    // 2558 - Method for ExpandableList Initialization
    private fun initializeExpandableListSetUp(caller: String) {
        if (expandableListView != null) {
            adapter = CustomModifyExpandableListAdapterDay(
                requireContext(),
                getString(R.string.day),
                titleDate,
                childData
            )
            expandableListView?.setAdapter(adapter)
            adapter?.setOnClickListener(this)
        }

        if (caller == AppConstants.INITIAL_DAY) {
            allDaysList.indexOf(fromDate).let { expandableListView?.expandGroup(it) }
            lastGroupPosition = allDaysList.indexOf(fromDate)
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onGroupClick(parent: ViewGroup, listPosition: Int, isExpanded: Boolean) {
        this.parent = parent as ExpandableListView
        this.groupPosition = listPosition
        fromDate = allDaysList[listPosition]
        toDate = allDaysList[listPosition]
        if (isExpanded) {
            parent.collapseGroup(listPosition)
        } else {
            // Send Selected Date To ViewModel For Calender UI Display
            fromDate?.let { sharedViewModel.setExpCurrentDate(it) }
            val bookedEventDetails = ArrayList<ListData>()
            bookedEventDetails.add(
                ListData(
                    getString(R.string.empty),
                    listOf(BookedEventServiceDto("", "", "", ""))
                )
            )
            childData[titleDate[groupPosition]] = bookedEventDetails
            parent.collapseGroup(lastGroupPosition)
            parent.expandGroup(listPosition)
            apiTokenValidation(AppConstants.BOOKED_EVENTS_SERVICES_FROM_SELECTED_DATE)
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
        when (branchName) {
            getString(R.string.available_small) -> {
                serviceVendorOnboardingId?.let { serviceVendorOnboardingId ->
                    fromDate?.let { fromDate ->
                        toDate?.let { toDate ->
                            DeselectingDialogFragment.newInstance(
                                AppConstants.DAY,
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
                                AppConstants.DAY,
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

    // 2814 - Method For getModifyDaySlot Api call with NULL
    private fun modifyNullApiUpdate(
        fromDate: String,
        isAvailable: Boolean,
        timeSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String
    ) {
        sharedViewModel.getModifyDaySlot(
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

    // 2670 - Method For Set IdToken And SpRegId From SharedPreferences
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        roleId = sharedPreference.getInt(SharedPreference.ROLE_ID)
    }

    // 2691 - method For Set Local Variable Value serviceCategoryId And ServiceVendorOnBoardingId
    private fun serviceCategoryIdAndServiceVendorOnboardingId(currentDate: ScheduleManagementViewModel.SelectedDate) {
        when {
            currentDate.seviceId == 0 -> {
                serviceCategoryId = null
                serviceVendorOnboardingId = null
            }
            currentDate.branchId == 0 -> {
                serviceCategoryId = currentDate.seviceId
                serviceVendorOnboardingId = null
            }
            else -> {
                serviceCategoryId = currentDate.seviceId
                serviceVendorOnboardingId = currentDate.branchId
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

    // 2670 - Method For Date And Month Arrangement To Display UI
    private fun dateFormat(input: String): String {
        var monthCount = input.substring(0, 2)
        val date = input.substring(3, 5)
        if (monthCount[0].digitToInt() == 0) {
            monthCount = monthCount[1].toString()
        }
        val month = Month.of(monthCount.toInt()).toString().substring(0, 3).let { month ->
            month.substring(0, 1) + month.substring(1, 2)
                .lowercase(Locale.getDefault()) + month.substring(2, 3)
                .lowercase(Locale.getDefault())
        }
        val currentDayFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
        val currentDay = LocalDate.parse(input, currentDayFormatter).dayOfWeek.getDisplayName(
            TextStyle.FULL,
            Locale.ENGLISH
        )
        return "$month  $date - $currentDay"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }
}