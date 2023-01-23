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
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.schedulemanagement.ScheduleManagementActivity
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
import kotlin.concurrent.schedule

class WeekExpandableListFragment : Fragment(),
    CustomExpandableListAdapter.TimeSlotIconClickListener, Tokens.IdTokenCallBackInterface {

    private var TAG = this::class.java.name
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
    private var weekMap: LinkedHashMap<Int, ArrayList<String>>? = null
    private var listOfDatesArray: ArrayList<ArrayList<String>> = ArrayList()
    private var isScroll: Boolean = false
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

    var listOfDates: ArrayList<String> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2670 - Initialize expendableList
        expandableListView = mDataBinding.expendableList
        // 2670 - Initialize Local Variables
        setIdTokenAndSpRegId()
        // 2670 - Token Class CallBack Initialization
        tokens.setCallBackInterface(this)

        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer weekexp")
            if (activity != null) {
                init()
            }
        }

        sharedViewModel.getCurrentWeekDate.observe(
            viewLifecycleOwner
        ) { currentWeekDate ->
            serviceCategoryIdAndServiceVendorOnboardingId(currentWeekDate)
            weekMap = getWeekListMap(currentWeekDate)
            listOfDatesArray.clear()
            weekMap?.forEach { it ->
                listOfDatesArray.add(it.value)
            }
            Log.d("TAG", "onViewCreated booked date: ${currentWeekDate.weekListMapOfMonth}")

            weekList = currentWeekDate.weekList
            Log.d("TAG", "onViewCreated booked datemap: $listOfDatesArray ${weekMap}")
            isScroll = currentWeekDate.isScroll
            listOfDates = currentWeekDate.bookedWeekList

            mDataBinding.modifyProgressBar.visibility = View.VISIBLE
            mDataBinding.expandableLayout.visibility = View.GONE

            listOfDatesArray.forEach {
                if (currentWeekDate.weekList[0] == it[0]) {
                    this.groupPosition = listOfDatesArray.indexOf(it)
                    fromDate = listOfDatesArray[groupPosition][0]
                    toDate =
                        listOfDatesArray[groupPosition][listOfDatesArray[groupPosition].lastIndex]
                }
            }
            setListOfDatesArray()
        }
    }

    // 2776 - Method For set week wise Dates ArrayList
    private fun setListOfDatesArray() {
        if (weekMap.isNullOrEmpty()) {
            mDataBinding.expendableList.visibility = View.GONE
            mDataBinding.modifyProgressBar.visibility = View.GONE
            mDataBinding.noEventsText.visibility = View.VISIBLE
        } else {
            mDataBinding.expendableList.visibility = View.VISIBLE
            mDataBinding.noEventsText.visibility = View.GONE
            init()
        }
    }

    private fun init() {
        apiTokenValidation(AppConstants.BOOKED_EVENT_SERVICES_INITIAL)
    }

    // 2670 - Method For Get Booked Event Services
    fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String,
        caller: String,
    ) {
        if (view != null) {
            sharedViewModel.getBookedEventServices(
                idToken, spRegId, serviceCategoryId,
                serviceVendorOnBoardingId,
                fromDate,
                toDate, AppConstants.WEEK
            ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        //  2986 Hiding progress based on calender and service selection
                        mDataBinding.modifyProgressBar.visibility = View.GONE
                        mDataBinding.expandableLayout.visibility = View.VISIBLE
                        Log.d("TAG", "check token response: ${apiResponse.response}")
                        if (caller == AppConstants.BOOKED_EVENT_SERVICES_INITIAL) {
                            eventsOnSelectedDateApiValueUpdate(apiResponse, caller)
                        } else {
                            setDataToExpandableList(apiResponse, groupPosition)
                            Log.d(TAG, "setDataToExpandableList: loop called")
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                    is ApisResponse.CustomError -> {
                        Log.d("TAG", "check token result: ${apiResponse.message}")
                    }
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

    private fun eventsOnSelectedDateApiValueUpdate(
        apiResponse: ApisResponse.Success<BookedServiceList>,
        caller: String,
    ) {
        childData.clear()
        titleDate.clear()
        Log.d(
            TAG,
            "eventsOnSelectedDateApiValueUpdate value: ${listOfDatesArray} ${apiResponse.response.data}"
        )
        for (i in 0 until listOfDatesArray.size) {
            Log.d(TAG, "eventsOnSelectedDateApiValueUpdate for: ${listOfDatesArray[i]}")
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
                    Log.d(TAG, "setDataToExpandableList: else called $groupPosition")
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
            titleDate.add(
                "${getMonth(listOfDatesArray[i][0])}  ${dateFormat(listOfDatesArray[i][0])} - ${
                    dateFormat(listOfDatesArray[i][listOfDatesArray[i].lastIndex])
                }"
            )
            childData[titleDate[i]] = bookedEventDetails
        }
        initializeExpandableListSetUp(caller)
    }

    private fun setDataToExpandableList(
        apiResponse: ApisResponse.Success<BookedServiceList>,
        groupPosition: Int
    ) {
        Log.d(TAG, "setDataToExpandableList: loop method $groupPosition")
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
                Log.d(TAG, "setDataToExpandableList: else called $groupPosition")
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

        childData[titleDate[groupPosition]] = bookedEventDetails
    }

    fun getOnlyBookedEvents(data: Data): ArrayList<BookedEventServiceDto> {
        val bookedEventServiceDtos = ArrayList<BookedEventServiceDto>()
        Log.d(TAG, "updateUpcomingEvents week: ${data.bookedEventServiceDtos}")
        val bookedList = ArrayList<BookedEventServiceDto>()
        data.bookedEventServiceDtos.forEach { objectList ->
            if (objectList.bidStatus == AppConstants.WON_BID) {
                bookedList.add(objectList)
            }
        }
        Log.d(TAG, "updateUpcomingEvents week: ${bookedList}")
        bookedEventServiceDtos.addAll(bookedList)
        return bookedEventServiceDtos
    }

    // 2558 - Method for ExpandableList Initialization
    fun initializeExpandableListSetUp(caller: String) {
        if (expandableListView != null) {
            adapter = CustomExpandableListAdapter(
                requireContext(),
                getString(R.string.week),
                titleDate,
                childData
            )
            expandableListView!!.setAdapter(adapter)
            adapter?.setOnClickListener(this)
        }

        // Condition For Expand selected Week TimeSlot
        if (caller == AppConstants.BOOKED_EVENT_SERVICES_INITIAL) {
            expandableListView!!.expandGroup(groupPosition)
            lastGroupPosition = groupPosition
            adapter?.notifyDataSetChanged()
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
    override fun onGroupClick(parent: ViewGroup, listPosition: Int, isExpanded: Boolean) {
        Log.d("TAG", "onGroupClick week: called ${listOfDatesArray[listPosition]}")
//        if (internetErrorDialogOld.checkInternetAvailable(requireContext())) {
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
                    listOf(BookedEventServiceDto("", "", "", "", ""))
                )
            )
            childData[titleDate[groupPosition]] = bookedEventDetails
            parent.collapseGroup(lastGroupPosition)
            parent.expandGroup(groupPosition)
            apiTokenValidation("bookedEventServicesFromSelectedDate")
        }
        lastGroupPosition = listPosition
//        }
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
            Log.d("TAG", "tokenCallBack: $fromDate $toDate")
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
    private fun getWeekListMap(currentWeekDate: ScheduleManagementViewModel.WeekDates): LinkedHashMap<Int, ArrayList<String>> {
        val weekMap = LinkedHashMap<Int, ArrayList<String>>()
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

    // 2670 - Method For Set IdToken And SpRegId From SharedPreferences
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        roleId = sharedPreference.getInt(SharedPreference.ROLE_ID)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onViewCreated: observe onDestroy: weekexp")
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }

}