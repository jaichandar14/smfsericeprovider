package com.smf.events.ui.schedulemanagement.calendarfragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
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

// 2458
class CalendarFragment : BaseFragment<FragmentCalendarBinding, ScheduleManagementViewModel>(),
    CalendarAdapter.OnItemListener, ScheduleManagementViewModel.CallBackInterface {

    @Inject
    lateinit var calendarUtils: CalendarUtils

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

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

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun getContentView(): Int = R.layout.fragment_calendar

    override fun getViewModel(): ScheduleManagementViewModel =
        ViewModelProvider(this, factory).get(ScheduleManagementViewModel::class.java)

    override fun getBindingVariable(): Int = BR.calendarManagementViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 2458 Method for Setting Id token and Reg Id
        setIdTokenAndSpRegId()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2458 Scheduled Management  ViewModel CallBackInterface
        getViewModel().setCallBackInterface(this)
        // 2458 Method for initializing
        initWidgets()
        // 2458 Method For Setting Month View in Calendar
        setMonthView()
        // 2458 Method for Setting Year
        getViewModel().year(mDataBinding, resources)
        // 2458 Method for All Service API call
        getAllServices()
    }

    // 2458 Method for initializing
    private fun initWidgets() {
        calendarRecyclerView = mDataBinding?.calendarRecyclerView
        monthYearText = mDataBinding?.monthYearTV
        CalendarUtils.selectedDate = LocalDate.now()
        // 2458 Method for  previousMonth
        previousMonthAction()
        // 2458 Method for  nextMonth
        nextMonthAction()
    }

    // 2458 Method For Setting Month View in Calendar
    private fun setMonthView() {
        monthYearText?.text =
            CalendarUtils.selectedDate?.let { calendarUtils.monthYearFromDate(it) }
        val thisYear: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (i in thisYear..2050) {
            resources.add(i.toString())
        }
        val daysInMonth: ArrayList<LocalDate>? = calendarUtils.daysInMonthArray()
        calendarAdapter = CalendarAdapter(daysInMonth, this, mDataBinding, "day")
        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(context, 7)
        calendarRecyclerView?.layoutManager = layoutManager
        calendarRecyclerView?.adapter = calendarAdapter
    }

    // 2458 Method for  previousMonth
    private fun previousMonthAction() {
        mDataBinding?.previousMonth?.setOnClickListener {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate?.minusMonths(1)
            setMonthView()
        }
    }

    // 2458 Method for  nextMonth
    private fun nextMonthAction() {
        mDataBinding?.nextMonth?.setOnClickListener {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate?.plusMonths(1)
            setMonthView()
        }
    }

    // 2458 Callback Method for Clicked Date
    override fun onItemClick(position: Int, date: LocalDate?) {
        if (date != null) {
            CalendarUtils.selectedDate = date
            setMonthView()
        }
    }

    // 2458 Getting All Service
    private fun getAllServices() {
        getViewModel().getAllServices(idToken, spRegId)
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
        getViewModel().allServices(mDataBinding, allServiceList)
    }

    // 2458 Branch ApiCall
    private fun getBranches(idToken: String, serviceCategoryId: Int) {
        getViewModel().getServicesBranches(idToken, spRegId, serviceCategoryId)
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
                        getViewModel().branches(
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
            getViewModel().branches(
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

    }
}