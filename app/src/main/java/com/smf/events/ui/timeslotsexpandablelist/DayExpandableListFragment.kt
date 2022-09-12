package com.smf.events.ui.timeslotsexpandablelist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
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
import com.smf.events.ui.timeslotsexpandablelist.adapter.CustomExpandableListAdapter
import com.smf.events.ui.timeslotsexpandablelist.model.BookedEventServiceDto
import com.smf.events.ui.timeslotsexpandablelist.model.BookedServiceList
import com.smf.events.ui.timeslotsexpandablelist.model.Data
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
import kotlin.concurrent.schedule

class DayExpandableListFragment : Fragment(),
    CustomExpandableListAdapter.TimeSlotIconClickListener, Tokens.IdTokenCallBackInterface,
    ScheduleManagementViewModel.CallBackExpListInterface {

    private var TAG = "DayExpandableListFragment"
    private var expandableListView: ExpandableListView? = null
    private var adapter: CustomExpandableListAdapter? = null
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
    private var selectedDate: String? = null
    var parent: ViewGroup? = null
    private var listOfDates: ArrayList<String>? = ArrayList()
    private var groupPosition: Int = 0
    private var isScroll: Boolean = false
    private lateinit var dialogDisposable: Disposable
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
        // 3061 Scheduled Management  ViewModel CallBackInterface
        sharedViewModel.setCallBackExpListInterface(this)
        // Internet Error Dialog Initialization
        internetErrorDialog = InternetErrorDialog.newInstance()

        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer dayexp")
            internetErrorDialog.dismissDialog()
            if (activity != null) {
                init()
            }
        }

        // 2558 - getDate ScheduleManagementViewModel Observer
        sharedViewModel.getCurrentDate.observe(viewLifecycleOwner, { currentDate ->
            serviceCategoryIdAndServiceVendorOnboardingId(currentDate)
            mDataBinding.modifyProgressBar.visibility = View.VISIBLE
            mDataBinding.expandableLayout.visibility = View.GONE
            val currentDayFormatter =
                DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT, Locale.ENGLISH)
            val currentDay = CalendarUtils.selectedDate?.format(currentDayFormatter)
            fromDate = currentDay
            toDate = currentDay
            selectedDate = currentDay
            listOfDates = currentDate.listOfDays
            isScroll = currentDate.isScroll
            Log.d(
                "TAG",
                "onViewCreated: ${CalendarUtils.selectedDate}, $fromDate $toDate $selectedDate $listOfDates"
            )
            setListOfDatesArrayList(currentDate)
        })
    }

    // 2776 - Method For set Dates ArrayList
    private fun setListOfDatesArrayList(currentDate: ScheduleManagementViewModel.SelectedDate) {
        if (currentDate.listOfDays.isNullOrEmpty()) {
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
        apiTokenValidation(AppConstants.BOOKED_EVENT_SERVICES_INITIAL)
    }

    // 2670 - Method For Get Booked Event Services
    private fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String,
        caller: String
    ) {
        if (view != null) {
            sharedViewModel.getBookedEventServices(
                idToken, spRegId, serviceCategoryId,
                serviceVendorOnBoardingId,
                fromDate,
                toDate, AppConstants.DAY
            ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        //  2986 Hiding progress based on calender and service selection
                        mDataBinding.modifyProgressBar.visibility = View.GONE
                        mDataBinding.expandableLayout.visibility = View.VISIBLE
                        Log.d(
                            "TAG",
                            "check token result success BookedEvent: ${apiResponse.response}"
                        )
                        if (caller == AppConstants.BOOKED_EVENT_SERVICES_INITIAL) {
                            updateExpandableListDataSelectedDate(apiResponse, caller)
                        } else {
                            setDataToExpandableList(apiResponse, groupPosition)
                            Log.d("TAG", "setDataToExpandableList: loop called")
                            adapter!!.notifyDataSetChanged()
                        }

                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result BookedEvent exp: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
        }
    }

    // 2558 - Method for ExpandableList Initialization
    private fun initializeExpandableListSetUp(caller: String) {
        if (expandableListView != null) {
            adapter = CustomExpandableListAdapter(
                requireContext(),
                getString(R.string.day),
                titleDate,
                childData
            )
            expandableListView!!.setAdapter(adapter)
            adapter?.setOnClickListener(this)
        }

        // Condition For Expand selected Week TimeSlot
        if (caller == AppConstants.BOOKED_EVENT_SERVICES_INITIAL) {
            if (listOfDates?.contains(fromDate) == true) {
                expandableListView!!.expandGroup(groupPosition)
                lastGroupPosition = groupPosition
                adapter?.notifyDataSetChanged()
            }
        }

        if (listOfDates?.contains(fromDate) == true) {
            if (isScroll) {
                // Condition for scroll to specific time slot location
                Timer().schedule(500) {
                    scrollToLocation()
                }
            }
        }

    }

    private fun scrollToLocation() {
        val position = listOfDates!!.indexOf(selectedDate)
        Log.d(TAG, "initializeExpandableListSetUp list: $listOfDates $position")
        Log.d(TAG, "expandableList full height: ${expandableListView?.height}")
        Log.d(
            TAG,
            "expandableList selected header height : ${position * expandableListView?.get(position)?.height!!}"
        )
        sharedViewModel.setScrollViewToPosition(position * expandableListView?.get(position)?.height!!)
    }

    override fun onGroupClick(parent: ViewGroup, listPosition: Int, isExpanded: Boolean) {
        if (internetErrorDialog.checkInternetAvailable(requireContext())) {
            this.parent = parent as ExpandableListView
            this.groupPosition = listPosition
            fromDate = listOfDates?.get(listPosition)
            toDate = listOfDates?.get(listPosition)
            if (isExpanded) {
                parent.collapseGroup(listPosition)
            } else {
                // Send Selected Date To ViewModel For Calender UI Display
                fromDate?.let { sharedViewModel.setExpCurrentDate(it) }
                val bookedEventDetails = ArrayList<ListData>()
                bookedEventDetails.add(
                    ListData(
                        getString(R.string.empty),
                        listOf(BookedEventServiceDto("", "", "", "", ""))
                    )
                )
                childData[titleDate[groupPosition]] = bookedEventDetails
                parent.collapseGroup(lastGroupPosition)
                parent.expandGroup(listPosition)
                apiTokenValidation("bookedEventServicesFromSelectedDate")
            }
            lastGroupPosition = listPosition
        }
    }

    // 2773 - Method For Update SelectedDate To ExpandableList
    private fun updateExpandableListDataSelectedDate(
        apiResponse: ApisResponse.Success<BookedServiceList>,
        caller: String
    ) {
        childData.clear()
        titleDate.clear()
        for (i in 0 until listOfDates!!.size) {
            Log.d("TAG", "eventsOnSelectedDateApiValueUpdate for: ${listOfDates!![i]}")
            val bookedEventDetails = ArrayList<ListData>()
            if (apiResponse.response.data.isNullOrEmpty()) {
                bookedEventDetails.add(
                    ListData(
                        "",
                        listOf(BookedEventServiceDto("", "", "", "", ""))
                    )
                )
            } else {
                apiResponse.response.data.forEach { it ->
                    Log.d("TAG", "setDataToExpandableList: else called $groupPosition")
                    val bookedEventServiceDtos = getOnlyBookedEvents(it)
                    if (!bookedEventServiceDtos.isNullOrEmpty()) {
                        bookedEventDetails.add(
                            ListData(
                                it.serviceSlot,
                                bookedEventServiceDtos
                            )
                        )
                    }
                }
            }
            if (bookedEventDetails.isNullOrEmpty()) {
                bookedEventDetails.add(
                    ListData(
                        "",
                        listOf(BookedEventServiceDto("", "", "", "", ""))
                    )
                )
            }
            listOfDates?.get(i)?.let { it -> dateFormat(it).let { titleDate.add(it) } }
            childData[titleDate[i]] = bookedEventDetails
        }
        // 2558 - ExpandableList Initialization
        initializeExpandableListSetUp(caller)
    }

    private fun getOnlyBookedEvents(data: Data): ArrayList<BookedEventServiceDto> {
        val bookedEventServiceDtos = ArrayList<BookedEventServiceDto>()
        Log.d("TAG", "updateUpcomingEvents week: ${data.bookedEventServiceDtos}")
        val bookedList = ArrayList<BookedEventServiceDto>()
        data.bookedEventServiceDtos.forEach { objectList ->
            if (objectList.bidStatus == AppConstants.WON_BID) {
                bookedList.add(objectList)
            }
        }
        Log.d("TAG", "updateUpcomingEvents week: ${bookedList}")
        bookedEventServiceDtos.addAll(bookedList)
        return bookedEventServiceDtos
    }

    // 2795 - Method For Set Data To ExpandableList
    private fun setDataToExpandableList(
        apiResponse: ApisResponse.Success<BookedServiceList>,
        position: Int
    ) {
        val bookedEventDetails = ArrayList<ListData>()
        if (apiResponse.response.data.isNullOrEmpty()) {
            bookedEventDetails.add(
                ListData(
                    "",
                    listOf(BookedEventServiceDto("", "", "", "", ""))
                )
            )
        } else {
            apiResponse.response.data.forEach { it ->
                Log.d("TAG", "setDataToExpandableList: else called $groupPosition")
                val bookedEventServiceDtos = getOnlyBookedEvents(it)
                if (!bookedEventServiceDtos.isNullOrEmpty()) {
                    bookedEventDetails.add(
                        ListData(
                            it.serviceSlot,
                            bookedEventServiceDtos
                        )
                    )
                }
            }
        }
        if (bookedEventDetails.isNullOrEmpty()) {
            bookedEventDetails.add(
                ListData(
                    "",
                    listOf(BookedEventServiceDto("", "", "", "", ""))
                )
            )
        }

        childData[titleDate[position]] = bookedEventDetails
    }

    override fun onClick(expandedListPosition: Int) {
//        TODO - Click Events
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
        val currentDayFormatter =
            DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT, Locale.ENGLISH)
        val currentDay = LocalDate.parse(input, currentDayFormatter).dayOfWeek.getDisplayName(
            TextStyle.FULL,
            Locale.ENGLISH
        )
        return "$month  $date - $currentDay"
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onViewCreated: observe onDestroy: dayexp")
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }

    override fun internetError(exception: String, tag: String) {
        Log.d(TAG, "internetError: called day")
        SharedPreference.isInternetConnected = false
        internetErrorDialog.checkInternetAvailable(requireContext())
    }

}