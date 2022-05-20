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
import com.smf.events.SMFApp
import com.smf.events.databinding.FragmentTimeSlotsExpandableListBinding
import com.smf.events.helper.*
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
    var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    private var fromDate: String? = null
    private var toDate: String? = null
    private var weekList: ArrayList<String>? = null

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

        sharedViewModel.getCurrentWeekDate.observe(requireActivity(),
            { currentWeekDate ->
                fromDate = currentWeekDate.fromDate
                toDate = currentWeekDate.toDate
                weekList = currentWeekDate.weekList
                serviceCategoryIdAndServiceVendorOnboardingId(currentWeekDate)
                // 2670 - Api Call Token Validation
                apiTokenValidation("bookedEventServices")
            })
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
                    weekList?.forEach {
                        if (it.format(dateFormatter) == LocalDate.now().format(dateFormatter)) {
                            fromDate = it.format(dateFormatter)
                        }
                    }
                    fromDate?.let { fromDate ->
                        toDate?.let { toDate ->
                            getBookedEventServices(
                                idToken, spRegId,
                                serviceCategoryId, serviceVendorOnboardingId,
                                fromDate, toDate
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }

    // 2670 - Method For Get Booked Event Services
    private fun getBookedEventServices(
        idToken: String, spRegId: Int, serviceCategoryId: Int?,
        serviceVendorOnBoardingId: Int?,
        fromDate: String,
        toDate: String
    ) {
        sharedViewModel.getBookedEventServices(
            idToken, spRegId, serviceCategoryId,
            serviceVendorOnBoardingId,
            fromDate,
            toDate
        ).observe(viewLifecycleOwner, androidx.lifecycle.Observer { apiResponse ->
            when (apiResponse) {
                is ApisResponse.Success -> {
                    Log.d("TAG", "check token response: ${apiResponse.response}")
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
            childData.put(titleDate[0], bookedEventDetails)
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
            childData.put(titleDate[0], bookedEventDetails)
        }
        // 2558 - ExpandableList Initialization
        initializeExpandableListSetUp()
    }

    // 2397 - Method For Add ExpandableList Title Group
    private fun addTitle() {
        titleDate.add(
            "${fromDate?.let { getMonth(it) }}  ${weekList?.get(0)?.let { dateFormat(it) }} - ${
                toDate?.let {
                    dateFormat(it)
                }
            }"
        )
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

    // 2558 - Method for ExpandableList Initialization
    private fun initializeExpandableListSetUp() {
        if (expandableListView != null) {
            adapter = CustomExpandableListAdapter(
                requireContext(),
                titleDate,
                childData
            )
            expandableListView!!.setAdapter(adapter)
            adapter?.setOnClickListener(this)

            expandableListView!!.setOnGroupClickListener { parent, v, groupPosition, id ->

                return@setOnGroupClickListener false
            }

            expandableListView!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->

                return@setOnChildClickListener false
            }

            expandableListView!!.setOnGroupCollapseListener { groupPosition ->
                Log.d(
                    "TAG",
                    "initializeExpandableListSetUp: clope $groupPosition"
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onViewCreated: TimeSlotsExpandableListFragment onResume called")
    }

    override fun onClick(expandedListPosition: Int) {
        Log.d("TAG", "onCreateView viewModel called $expandedListPosition")
    }

    // 2670 - Method For Set IdToken And SpRegId From SharedPreferences
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        roleId = sharedPreference.getInt(SharedPreference.ROLE_ID)
    }
}