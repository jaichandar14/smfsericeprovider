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
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.timeslotmodifyexpanablelist.adapter.CustomModifyExpandableListAdapter
import com.smf.events.ui.timeslotsexpandablelist.model.BookedEventServiceDto
import com.smf.events.ui.timeslotsexpandablelist.model.BookedServiceList
import com.smf.events.ui.timeslotsexpandablelist.model.ListData
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MonthModifyExpandableListFragment : Fragment(),
    CustomModifyExpandableListAdapter.TimeSlotIconClickListener, Tokens.IdTokenCallBackInterface {

    private var expandableListView: ExpandableListView? = null
    private var adapter: CustomModifyExpandableListAdapter? = null
    private var childData = HashMap<String, List<ListData>>()
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
        sharedViewModel.getCurrentMonthDate.observe(requireActivity(),
            { currentMonthDate ->
                fromDate = currentMonthDate.fromDate
                toDate = currentMonthDate.toDate
                currentDate = currentMonthDate.currentDate
                monthValue = currentMonthDate.monthValue.toString()
                serviceCategoryIdAndServiceVendorOnboardingId(currentMonthDate)
                Log.d("TAG", "onViewCreated monthValue: $monthValue")
                monthValidation()
            })
    }

    // 2795 - Method For Restrict Previous Month
    private fun monthValidation() {
        if (!fromDate.isNullOrEmpty() && !toDate.isNullOrEmpty()) {
            mDataBinding.expendableList.visibility = View.VISIBLE
            mDataBinding.noEventsText.visibility = View.GONE
            // 2670 - Api Call Token Validation
            apiTokenValidation("bookedEventServices")
        } else {
            mDataBinding.expendableList.visibility = View.GONE
            mDataBinding.noEventsText.visibility = View.VISIBLE
        }
    }

    // 2670 - Method For Get Booked Event Services
    private fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String,
    ) {
        sharedViewModel.getBookedEventServices(
            idToken, spRegId, serviceCategoryId,
            serviceVendorOnBoardingId,
            fromDate,
            toDate
        ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
            when (apiResponse) {
                is ApisResponse.Success -> {
                    Log.d("TAG", "check token result success month: ${apiResponse.response.data}")
                    updateExpandableListData(apiResponse)
                }
                is ApisResponse.Error -> {
                    Log.d("TAG", "check token result: ${apiResponse.exception}")
                }
                else -> {
                }
            }
        })
    }

    // Method For Updating ExpandableList Data
    private fun updateExpandableListData(apiResponse: ApisResponse.Success<BookedServiceList>) {
        val bookedEventDetails = ArrayList<ListData>()
        if (apiResponse.response.data.isNullOrEmpty()) {
            childData.clear()
            titleDate.clear()
            addTitle()
            bookedEventDetails.add(ListData("", listOf(BookedEventServiceDto("", "", "", ""))))
            childData[titleDate[0]] = bookedEventDetails
        } else {
            childData.clear()
            titleDate.clear()
            addTitle()
            for (i in apiResponse.response.data.indices) {
                bookedEventDetails.add(
                    ListData(
                        apiResponse.response.data[i].serviceSlot,
                        apiResponse.response.data[i].bookedEventServiceDtos
                    )
                )
            }
            childData[titleDate[0]] = bookedEventDetails
        }
        // 2558 - ExpandableList Initialization
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

            // Default Expansion Into The ExpandableList
            expandableListView!!.expandGroup(groupPosition)
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun onGroupClick(parent: ViewGroup, listPosition: Int, isExpanded: Boolean) {
        Log.d("TAG", "onGroupClick month: called")
    }

    override fun onClick(expandedListPosition: Int) {
        Log.d("TAG", "onCreateView viewModel called $expandedListPosition")
//        TODO - Click Events
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
        withContext(Dispatchers.Main) {
            when (caller) {
                "bookedEventServices" -> {
                    val currentMonthValue = LocalDateTime.now().monthValue.toString()
                    currentDate = if (monthValue == currentMonthValue) {
                        currentDate
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
                else -> {
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

}