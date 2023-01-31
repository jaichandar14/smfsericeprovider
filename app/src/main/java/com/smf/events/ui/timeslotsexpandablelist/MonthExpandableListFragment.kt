package com.smf.events.ui.timeslotsexpandablelist

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
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

class MonthExpandableListFragment : Fragment(), Tokens.IdTokenCallBackInterface {

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
    private val currentDayFormatter =
        DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT, Locale.ENGLISH)
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

        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer monthexp")
            activity?.let {
                init()
            }
        }
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
            serviceCategoryIdAndServiceVendorOnboardingId(currentMonthDate)
            Log.d("TAG", "onViewCreated monthValue: $monthValue")
            monthValidation()
        }
    }

    // 2795 - Method For Restrict Previous Month
    private fun monthValidation() {
        if (!fromDate.isNullOrEmpty() && !toDate.isNullOrEmpty()) {
            mDataBinding.expendableList.visibility = View.VISIBLE
            mDataBinding.noEventsText.visibility = View.GONE
            init()
        } else {
            mDataBinding.expendableList.visibility = View.GONE
            mDataBinding.modifyProgressBar.visibility = View.GONE
            mDataBinding.noEventsText.visibility = View.VISIBLE
        }
    }

    private fun init() {
        // 2670 - Api Call Token Validation
        apiTokenValidation(AppConstants.BOOKED_EVENT_SERVICES)
    }

    // 2670 - Method For Get Booked Event Services
    fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String,
    ) {
        view?.let {
            sharedViewModel.getBookedEventServices(
                idToken, spRegId, serviceCategoryId,
                serviceVendorOnBoardingId,
                fromDate,
                toDate, AppConstants.MONTH
            ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        //  2986 Hiding progress based on calender and service selection
                        mDataBinding.modifyProgressBar.visibility = View.GONE
                        mDataBinding.expandableLayout.visibility = View.VISIBLE
                        Log.d(
                            "TAG",
                            "check token result success month: ${apiResponse.response.data}"
                        )
                        updateExpandableListData(apiResponse)
                    }
                    is ApisResponse.CustomError -> {
                        Log.d("TAG", "check token result: ${apiResponse.message}")
                        Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_SHORT)
                            .show()
                        mDataBinding.modifyProgressBar.visibility = View.GONE
                        mDataBinding.expandableLayout.visibility = View.VISIBLE
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

    // Method For Updating ExpandableList Data
    private fun updateExpandableListData(apiResponse: ApisResponse.Success<BookedServiceList>) {
        childData.clear()
        titleDate.clear()
        val bookedEventDetails = ArrayList<ListData>()
        if (apiResponse.response.data.isEmpty()) {
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
                if (bookedEventServiceDtos.isNotEmpty()) {
                    bookedEventDetails.add(
                        ListData(
                            it.serviceSlot,
                            bookedEventServiceDtos
                        )
                    )
                }
            }

            if (bookedEventDetails.isEmpty()) {
                bookedEventDetails.add(
                    ListData(
                        "",
                        listOf(BookedEventServiceDto("", "", "", "", ""))
                    )
                )
            }
        }

        addTitle()
        childData[titleDate[0]] = bookedEventDetails

        // 2558 - ExpandableList Initialization
        initializeExpandableListSetUp()
    }

    fun getOnlyBookedEvents(data: Data): ArrayList<BookedEventServiceDto> {
        val bookedEventServiceDtos = ArrayList<BookedEventServiceDto>()
        Log.d("TAG", "updateUpcomingEvents week: ${data.bookedEventServiceDtos}")
        val bookedList = ArrayList<BookedEventServiceDto>().apply {
            data.bookedEventServiceDtos.forEach { objectList ->
                if (objectList.bidStatus == AppConstants.WON_BID) {
                    this.add(objectList)
                }
            }
        }
        Log.d("TAG", "updateUpcomingEvents week: ${bookedList}")
        bookedEventServiceDtos.addAll(bookedList)
        return bookedEventServiceDtos
    }

    // 2558 - Method for ExpandableList Initialization
    private fun initializeExpandableListSetUp() {
        if (expandableListView != null) {
            adapter = CustomExpandableListAdapter(
                requireContext(),
                getString(R.string.month),
                titleDate,
                childData
            )
            expandableListView!!.setAdapter(adapter)

            // Default Expansion Into The ExpandableList
            expandableListView!!.expandGroup(groupPosition)
            adapter!!.notifyDataSetChanged()
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
                when (caller) {
                    AppConstants.BOOKED_EVENT_SERVICES -> {
                        val currentMonthValue = LocalDateTime.now().monthValue.toString()
                        currentDate = if (monthValue == currentMonthValue) {
                            val currentDay = LocalDateTime.now().format(currentDayFormatter)
                            currentDay
                        } else {
                            fromDate
                        }
                        currentDate?.let { currentDate ->
                            toDate?.let { toDate ->
                                getBookedEventServices(
                                    idToken, spRegId,
                                    serviceCategoryId, serviceVendorOnboardingId,
                                    currentDate, toDate
                                )
                            }
                        }
                    }
                    else -> {}
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onViewCreated: observe onDestroy: monthexp")
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }

}