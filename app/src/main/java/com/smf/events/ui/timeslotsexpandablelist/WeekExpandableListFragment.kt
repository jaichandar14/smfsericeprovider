package com.smf.events.ui.timeslotsexpandablelist

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
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslotsexpandablelist.adapter.CustomExpandableListAdapter
import com.smf.events.ui.timeslotsexpandablelist.model.BookedEventServiceDto
import com.smf.events.ui.timeslotsexpandablelist.model.BookedServiceList
import com.smf.events.ui.timeslotsexpandablelist.model.ListData
import dagger.android.support.AndroidSupportInjection
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

class WeekExpandableListFragment : Fragment(),
    CustomExpandableListAdapter.TimeSlotIconClickListener, Tokens.IdTokenCallBackInterface {

    private var expandableListView: ExpandableListView? = null
    private var adapter: CustomExpandableListAdapter? = null
    private var childData = HashMap<String, List<ListData>>()
    private var titleDate = ArrayList<String>()
    private lateinit var mDataBinding: FragmentTimeSlotsExpandableListBinding
    var spRegId: Int = 0
    lateinit var idToken: String
    var roleId: Int = 0
    var serviceCategoryId: Int? = null
    var serviceVendorOnboardingId: Int? = null
    var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT)
    private var fromDate: String? = null
    private var toDate: String? = null
    private var weekList: ArrayList<String>? = null
    var parent: ViewGroup? = null
    private var groupPosition: Int = 0
    private var weekMap: HashMap<Int, ArrayList<String>>? = null
    private var listOfDatesArray: ArrayList<ArrayList<String>> = ArrayList()

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

    var listOfDates: ArrayList<String> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2670 - Initialize expendableList
        expandableListView = mDataBinding.expendableList
        // 2670 - Initialize Local Variables
        setIdTokenAndSpRegId()
        // 2670 - Token Class CallBack Initialization
        tokens.setCallBackInterface(this)

        sharedViewModel.getCurrentWeekDate.observe(viewLifecycleOwner,
            { currentWeekDate ->
                serviceCategoryIdAndServiceVendorOnboardingId(currentWeekDate)
                weekMap = getWeekListMap(currentWeekDate)
                listOfDatesArray.clear()
                weekMap?.forEach { it ->
                    listOfDatesArray.add(it.value)
                }
                Log.d("TAG", "onViewCreated booked date: ${currentWeekDate.weekListMapOfMonth}")

                weekList = currentWeekDate.weekList
                Log.d("TAG", "onViewCreated booked datemap: ${weekMap}")

                listOfDates = currentWeekDate.bookedWeekList

                if (checkCurrentWeekHaveEvent(currentWeekDate)) {
                    mDataBinding.modifyProgressBar.visibility = View.VISIBLE
                    mDataBinding.expandableLayout.visibility= View.GONE
                    mDataBinding.noEventsText.visibility = View.GONE
                    listOfDatesArray.forEach {
                        if (currentWeekDate.weekList[0] == it[0]) {
                            this.groupPosition = listOfDatesArray.indexOf(it)
                            fromDate = listOfDatesArray[groupPosition][0]
                            toDate =
                                listOfDatesArray[groupPosition][listOfDatesArray[groupPosition].lastIndex]
                            expandableListInitialSetUpWithEvents()
                        }
                    }
                } else {
                    listOfDatesArray.forEach {
                        if (currentWeekDate.weekList[0] == it[0]) {
                            this.groupPosition = listOfDatesArray.indexOf(it)
                        }
                    }
                    setListOfDatesArray()
                }
            })
    }

    // 2795 - Method For Check Current Week Booked Events
    private fun checkCurrentWeekHaveEvent(currentWeekDate: ScheduleManagementViewModel.WeekDates): Boolean {
        var status = false
        listOfDates.forEach {
            if (currentWeekDate.weekList.contains(it)) {
                status = true
            }
        }
        return status
    }

    // 2776 - Method For set week wise Dates ArrayList
    private fun setListOfDatesArray() {
        if (weekMap.isNullOrEmpty()) {
            mDataBinding.expandableLayout.visibility = View.GONE
//            mDataBinding.expendableList.visibility = View.GONE
            mDataBinding.noEventsText.visibility = View.VISIBLE
        } else {
            mDataBinding.expandableLayout.visibility = View.VISIBLE
//            mDataBinding.expendableList.visibility = View.VISIBLE
            mDataBinding.noEventsText.visibility = View.GONE
            expandableListInitialSetUp()
        }
    }

    // 2776 -  Method For Set titleDate and childData values
    private fun expandableListInitialSetUp() {
        setExpandableListEmptyData()
        initializeExpandableListSetUp()
    }

    // 2795 -  Method For Set titleDate and childData values
    private fun expandableListInitialSetUpWithEvents() {
        setExpandableListEmptyData()
        apiTokenValidation(AppConstants.BOOKED_EVENT_SERVICES_INITIAL)
    }

    // 2795 - Method For Set Expandable List Data
    private fun setExpandableListEmptyData() {
        childData.clear()
        titleDate.clear()
        for (listOfDays in 0 until listOfDatesArray.size) {
            Log.d("TAG", "onViewCreated startingDate : ${listOfDatesArray[listOfDays][0]}")
            val bookedEventDetails = ArrayList<ListData>()
            bookedEventDetails.add(ListData("", listOf(BookedEventServiceDto("", "", "", ""))))
            titleDate.add(
                "${getMonth(listOfDatesArray[listOfDays][0])}  ${dateFormat(listOfDatesArray[listOfDays][0])} - ${
                    dateFormat(listOfDatesArray[listOfDays][listOfDatesArray[listOfDays].lastIndex])
                }"
            )
            childData[titleDate[listOfDays]] = bookedEventDetails
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
        if (view != null){
            sharedViewModel.getBookedEventServices(
                idToken, spRegId, serviceCategoryId,
                serviceVendorOnBoardingId,
                fromDate,
                toDate
            ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        //  2986 Hiding progress based on calender and service selection
                        mDataBinding.modifyProgressBar.visibility = View.GONE
                        mDataBinding.expandableLayout.visibility = View.VISIBLE
                        mDataBinding.noEventsText.visibility = View.GONE
                        Log.d("TAG", "check token response: ${apiResponse.response}")
                        updateExpandableListData(apiResponse, caller)
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
        }
    }

    // Method For Updating ExpandableList Data
    private fun updateExpandableListData(
        apiResponse: ApisResponse.Success<BookedServiceList>,
        caller: String,
    ) {
        if (caller == AppConstants.BOOKED_EVENT_SERVICES_INITIAL) {
            // ExpandableView And Adapter Initialization
            initializeExpandableListSetUp()
            updateApiDataToExpandableView(apiResponse)
            expandableListView?.expandGroup(groupPosition)
            adapter!!.notifyDataSetChanged()
            lastGroupPosition = groupPosition
        } else {
            updateApiDataToExpandableView(apiResponse)
            adapter!!.notifyDataSetChanged()
        }

    }

    // 2795 - Method For Update Api Data To Expandable List View
    private fun updateApiDataToExpandableView(apiResponse: ApisResponse.Success<BookedServiceList>) {
        val bookedEventDetails = ArrayList<ListData>()
        if (apiResponse.response.data.isNullOrEmpty()) {
            bookedEventDetails.add(ListData("", listOf(BookedEventServiceDto("", "", "", ""))))
            childData[titleDate[groupPosition]] = bookedEventDetails
        } else {
            for (i in apiResponse.response.data.indices) {
                bookedEventDetails.add(
                    ListData(
                        apiResponse.response.data[i].serviceSlot,
                        apiResponse.response.data[i].bookedEventServiceDtos
                    )
                )
            }
            childData[titleDate[groupPosition]] = bookedEventDetails
        }
    }

    // 2558 - Method for ExpandableList Initialization
    private fun initializeExpandableListSetUp() {
        if (expandableListView != null) {
            adapter = CustomExpandableListAdapter(
                requireContext(),
                getString(R.string.week),
                titleDate,
                childData
            )
            expandableListView!!.setAdapter(adapter)
            expandableListView!!.expandGroup(groupPosition)
            adapter?.setOnClickListener(this)
            lastGroupPosition = groupPosition
        }
    }

    // 2776 -  Method For Perform Group Click Events
    override fun onGroupClick(parent: ViewGroup, listPosition: Int, isExpanded: Boolean) {
        Log.d("TAG", "onGroupClick week: called ${listOfDatesArray[listPosition]}")
        this.parent = parent as ExpandableListView
        this.groupPosition = listPosition
        this.weekList = listOfDatesArray[listPosition]
        fromDate = listOfDatesArray[listPosition][0]
        toDate = listOfDatesArray[listPosition][listOfDatesArray[listPosition].lastIndex]
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
            apiTokenValidation("bookedEventServicesFromSelectedDate")
        }
        lastGroupPosition = listPosition
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
            weekList?.forEach {
                if (it.format(dateFormatter) == LocalDate.now().format(dateFormatter)) {
                    fromDate = it.format(dateFormatter)
                }
            }
            Log.d("TAG", "tokenCallBack: $fromDate")
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
            Log.d("TAG", "getWeekListMap: ${it.key}")
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

    override fun onClick(expandedListPosition: Int) {
        Log.d("TAG", "onCreateView viewModel called $expandedListPosition")
//        TODO - Click Events
    }

    // 2670 - Method For Set IdToken And SpRegId From SharedPreferences
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        roleId = sharedPreference.getInt(SharedPreference.ROLE_ID)
    }
}