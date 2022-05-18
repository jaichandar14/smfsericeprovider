package com.smf.events.ui.schedulemanagement.calendarfragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.databinding.FragmentCalendarBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.CalendarUtils
import com.smf.events.helper.SharedPreference
import com.smf.events.ui.dashboard.model.BranchDatas
import com.smf.events.ui.dashboard.model.DatasNew
import com.smf.events.ui.dashboard.model.ServicesData
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.schedulemanagement.adapter.CalendarAdapter
import dagger.android.support.AndroidSupportInjection
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

// 2458
class CalendarFragment : Fragment(),
    CalendarAdapter.OnItemListener, ScheduleManagementViewModel.CallBackInterface {
    @Inject
    lateinit var calendarUtils: CalendarUtils

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    // SharedViewModel Initialization
    private val sharedViewModel: ScheduleManagementViewModel by activityViewModels()

    private lateinit var mDataBinding: FragmentCalendarBinding
    var calendarFormat: String? = null
    var serviceList = ArrayList<ServicesData>()
    var serviceCategoryId: Int = 0
    var serviceVendorOnboardingId: Int = 0
    var branchListSpinner = ArrayList<BranchDatas>()
    var resources: ArrayList<String> = ArrayList()
    lateinit var calendarAdapter: CalendarAdapter
    var spRegId: Int = 0
    lateinit var idToken: String
    private var monthYearText: TextView? = null
    private var calendarRecyclerView: RecyclerView? = null
    var dayinWeek: ArrayList<String>? = null
    var daysPositon: ArrayList<Int>? = null
    var absoluteAdapterPosition:Int?=null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 2458 Method for Setting Id token and Reg Id
        setIdTokenAndSpRegId()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mDataBinding = FragmentCalendarBinding.inflate(inflater, container, false)
        return mDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2458 Scheduled Management  ViewModel CallBackInterface
        sharedViewModel.setCallBackInterface(this)
        // 2458 Method for initializing
        initWidgets()
        // 2622 Method for Calendar Format(Day,week,month) Picker
        setCalendarFormat()
        // 2458 Method for Setting Year
        sharedViewModel.year(mDataBinding, resources)
        // 2458 Method for All Service API call
        getAllServices()

    }

    // 2458 Method for initializing
    private fun initWidgets() {
        calendarRecyclerView = mDataBinding.calendarRecyclerView
        monthYearText = mDataBinding.monthYearTV
        CalendarUtils.selectedDate = LocalDate.now()
        // 2458 Method for  previousMonth
        previousMonthAction()
        // 2458 Method for  nextMonth
        nextMonthAction()
    }

    // 2622 Method for Calendar Format(Day,week,month) Picker
    private fun setCalendarFormat() {
        if (calendarFormat.isNullOrEmpty()) {
            calendarFormat = "Day"
            setMonthView(dayinWeek, daysPositon)
        }
        sharedViewModel.getCalendarFormat.observe(requireActivity(), {
            Log.d("TAG", "onCreateView viewModel called CalendarFragment: $it")
            calendarFormat = it
            // 2458 Method For Setting Month View in Calendar
            setMonthView(dayinWeek, daysPositon)
        })
    }


    //2622
// 2458 Method For Setting Month View in Calendar
    private fun setMonthView(dayinWeek: ArrayList<String>?, daysPositon: ArrayList<Int>?) {
        monthYearText?.text =
            CalendarUtils.selectedDate?.let { calendarUtils.monthYearFromDate(it) }
        val thisYear: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (i in thisYear..2050) {
            resources.add(i.toString())
        }
        Log.d("TAG", "setMonthViewMonth:$calendarFormat ")
        val daysInMonth: ArrayList<LocalDate>? = calendarUtils.daysInMonthArray()
        var calendarType = String()
        when (calendarFormat) {
            "Day" -> {
                calendarType = "Day"
            }
            "Week" -> {
                calendarType = "Week"
            }
            "Month" -> {
                calendarType = "Month"
            }
        }
        calendarAdapter =
            CalendarAdapter(daysInMonth, this, mDataBinding, calendarType, dayinWeek,
                daysPositon)
        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(context, 7)
        calendarRecyclerView?.layoutManager = layoutManager
        calendarRecyclerView?.adapter = calendarAdapter
    }


    // 2458 Method for  previousMonth
    private fun previousMonthAction() {
        mDataBinding.previousMonth.setOnClickListener {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate?.minusMonths(1)
            daysPositon=null
            Log.d("TAG", "previousMonthAction: ${CalendarUtils.selectedDate}")
            setMonthView(dayinWeek, daysPositon)
        }
    }

    // 2458 Method for  nextMonth
    private fun nextMonthAction() {
        mDataBinding.nextMonth.setOnClickListener {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate?.plusMonths(1)
            daysPositon=null
            setMonthView(dayinWeek, daysPositon)
        }
    }

    // 2458 Callback Method for Clicked Date
    override fun onItemClick(
        position: Int,
        date: LocalDate?,
        dayinWeek: ArrayList<String>,
        daysPositon: ArrayList<Int>,
    ) {
        if (date != null) {
            CalendarUtils.selectedDate = date
            setMonthView(dayinWeek, daysPositon)
        }
    }

    override fun weekSelection(
        pos: ArrayList<Int>,
        selectedDate: LocalDate?,
        absoluteAdapterPosition: Int
    ) {
        if (selectedDate != null) {
            CalendarUtils.selectedDate = selectedDate
            Log.d("TAG", "weekSelectioninfrag: $pos")
            daysPositon = pos
            this.absoluteAdapterPosition=absoluteAdapterPosition
            // setMonthView(dayinWeek, pos)
        }
    }

    // 2458 Getting All Service
    private fun getAllServices() {
        sharedViewModel.getAllServices(idToken, spRegId)
            .observe(viewLifecycleOwner, { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        serviceList.add(ServicesData("All Service", 0))
                        branchListSpinner.add(BranchDatas("Branches", 0))
                        apiResponse.response.data.forEach {
                            serviceList.add(ServicesData(it.serviceName, it.serviceCategoryId))
                        }
                        // 2458 Setting All Service
                        setAllService()
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
    }

    // 2458 Setting All Service
    private fun setAllService() {
        val allServiceList: ArrayList<String> = ArrayList()
        serviceList.forEach {
            allServiceList.add(it.serviceName)
        }
        // 2458 spinner view for all Services
        sharedViewModel.allServices(mDataBinding, allServiceList)
    }

    // 2458 Branch ApiCall
    private fun getBranches(idToken: String, serviceCategoryId: Int) {
        sharedViewModel.getServicesBranches(idToken, spRegId, serviceCategoryId)
            .observe(this, { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        var branchTypeItems: List<DatasNew> = apiResponse.response.datas
                        branchListSpinner.add(BranchDatas("Branches", 0))
                        for (i in branchTypeItems.indices) {
                            val branchName: String =
                                branchTypeItems[i].branchName
                            // 2458I want to show this when Selected
                            val branchId: Int = branchTypeItems[i].serviceVendorOnboardingId
                            branchListSpinner.add(BranchDatas(branchName, branchId))
                        }
                        var branchList: ArrayList<String> = ArrayList()
                        for (i in branchListSpinner.indices) {
                            val branchName: String =
                                branchListSpinner[i].branchName // 2458 I want to show this when Selected
                            branchList.add(branchName)
                        }
                        val branchData = branchList
                        sharedViewModel.branches(
                            mDataBinding,
                            branchData,
                            this.idToken,
                            spRegId,
                            serviceCategoryId,
                            0
                        )
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
    }

    // 2458 Setting IdToken, SpRegId And RollId
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
    }

    // 2458 CallBack for Clicked All Service Items
    override fun itemClick(position: Int) {
        if (serviceList[position].serviceName == "All Service") {
            var branchSpinner: ArrayList<String> = ArrayList()
            branchSpinner.add(0, "Branches")
            serviceCategoryId = 0
            val branchDataSpinner = branchSpinner
            sharedViewModel.branches(
                mDataBinding,
                branchDataSpinner,
                idToken,
                spRegId,
                serviceCategoryId,
                0
            )
        }
        if (serviceList[position].serviceName != "All Service") {
            serviceCategoryId = (serviceList[position].serviceCategoryId)
            getBranches(idToken, serviceCategoryId)
            branchListSpinner.clear()
        }
    }

    // 2458 CallBack for Clicked All Service Items
    override fun branchItemClick(
        serviceVendorOnboardingId: Int,
        name: String?,
        allServiceposition: Int?,
    ) {
        this.serviceVendorOnboardingId = branchListSpinner[serviceVendorOnboardingId].branchId
        var branchesName = name
        if (branchListSpinner[serviceVendorOnboardingId].branchId == 0) {
            branchesName = "Branches"
        }
        var serviceName = (serviceList[allServiceposition!!].serviceName)
        Log.d("TAG",
            "branchItemClick: serviceid: ${serviceCategoryId} branchid:${branchListSpinner[serviceVendorOnboardingId].branchId}")
        EventDateAndCounts(serviceCategoryId, branchListSpinner[serviceVendorOnboardingId].branchId)
    }

    private fun EventDateAndCounts(serviceCategoryId: Int?, branchId: Int) {
        var serviceId: Int? = null
        var branchesId: Int? = null
        if (serviceCategoryId == 0) {
            serviceId = null
        } else if (branchId == 0) {
            branchesId = null
        } else {
            serviceId = serviceCategoryId
            branchesId = branchId
        }
        sharedViewModel.getEventDates(idToken,
            spRegId,
            serviceId,
            branchesId,
            "05/20/2022",
            "05/20/2022").observe(viewLifecycleOwner, { apiresponse ->
            when (apiresponse) {
                is ApisResponse.Success -> {
                    Log.d("TAG", "check token result: ${apiresponse.response.data.dayCount}")
                }
                is ApisResponse.Error -> {
                    Log.d("TAG", "check token result: ${apiresponse.exception}")
                }
            }
        })
    }

}