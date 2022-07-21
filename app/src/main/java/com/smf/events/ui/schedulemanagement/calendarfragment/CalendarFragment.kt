package com.smf.events.ui.schedulemanagement.calendarfragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.SMFApp
import com.smf.events.databinding.FragmentCalendarBinding
import com.smf.events.helper.*
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.dashboard.model.BranchDatas
import com.smf.events.ui.dashboard.model.DatasNew
import com.smf.events.ui.dashboard.model.ServicesData
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import com.smf.events.ui.schedulemanagement.adapter.CalendarAdapter
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// 2458
class CalendarFragment : Fragment(),
    CalendarAdapter.OnItemListener, ScheduleManagementViewModel.CallBackInterface,
    Tokens.IdTokenCallBackInterface {
    @Inject
    lateinit var calendarUtils: CalendarUtils

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var tokens: Tokens

    // SharedViewModel Initialization
    private val sharedViewModel: ScheduleManagementViewModel by activityViewModels()

    private lateinit var mDataBinding: FragmentCalendarBinding
    private var calendarFormat: String? = null
    private var serviceList = ArrayList<ServicesData>()
    var serviceCategoryId: Int = 0
    var serviceVendorOnboardingId: Int = 0
    private var branchListSpinner = ArrayList<BranchDatas>()
    var resources: ArrayList<String> = ArrayList()
    private lateinit var calendarAdapter: CalendarAdapter
    var spRegId: Int = 0
    lateinit var idToken: String
    private var monthYearText: TextView? = null
    private var fullMonthYearText: TextView? = null
    private var yearText: TextView? = null
    private var calendarRecyclerView: RecyclerView? = null
    private var dayinWeek: ArrayList<String>? = null
    private var daysPositon: ArrayList<Int>? = null
    private var absoluteAdapterPosition: Int? = null
    var serviceDate = ArrayList<String>()
    private val c: Calendar = Calendar.getInstance()
    private val cmonth = c.get(Calendar.MONTH)
    private val cyear = c.get(Calendar.YEAR)
    private var weekSelectedListAll: HashMap<LocalDate, Int>? = HashMap()
    private var dateList: ArrayList<String> = ArrayList()
    private var businessValidity: LocalDate? = null
    lateinit var dialogDisposable: Disposable
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
        mDataBinding.progressBar.visibility = View.VISIBLE
        mDataBinding.calendarLayout.visibility = View.INVISIBLE
        // 2686 token CallBackInterface
        tokens.setCallBackInterface(this)
        // 2458 Scheduled Management  ViewModel CallBackInterface
        sharedViewModel.setCallBackInterface(this)
        // 2458 Method for initializing
        initWidgets()
        // 2622 Method for Calendar Format(Day,week,month) Picker
        setCalendarFormat()
        // 2686 Method for Token Validation and Service list ApiCall
        apiTokenValidationCalendar("AllServices")
        // 2796 Method for observing Exp View Date
        selectedEXPDateObserver()
        mDataBinding.closeCalendar.setOnClickListener {
            RxBus.publish(RxEvent.ChangingNav(1))
        }
        dialogDisposable = RxBus.listen(RxEvent.IsValid::class.java).subscribe {
            mDataBinding.progressBar.visibility = View.GONE
            mDataBinding.calendarLayout.visibility = View.VISIBLE
        }
// 2985
        getBusinessValiditiy()
    }

    private fun getBusinessValiditiy() {
        sharedViewModel.getBusinessValiditiy(idToken, spRegId)
            .observe(viewLifecycleOwner, { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        val currentDayFormatter =
                            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
                        val businessValidationDate =
                            LocalDate.parse(apiResponse.response.data.toDate, currentDayFormatter)
                        Log.d("TAG", "getBusinessValiditiy: $businessValidationDate")
                        businessValidity = businessValidationDate
                        CalendarUtils.businessValidity = businessValidationDate
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Timeout", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun selectedEXPDateObserver() {
        sharedViewModel.getExpCurrentDate.observe(requireActivity(), {
            val currentDayFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
            val currentDay = LocalDate.parse(it, currentDayFormatter)
            CalendarUtils.selectedDate = currentDay
            setMonthView(dayinWeek, daysPositon)
        })
    }

    private fun setSelectedWeekSetter(
        weeksOfMonth: java.util.HashMap<Int, WeekDatesOfMonth>,
        fromAndToDate: CalendarUtils.WeekDates,
    ) {
        sharedViewModel.setCurrentWeekDate(
            weeksOfMonth,
            serviceCategoryId,
            serviceVendorOnboardingId,
            fromAndToDate.weekList, serviceDate
        )

    }

    // 2458 Method for initializing
    private fun initWidgets() {
        calendarRecyclerView = mDataBinding.calendarRecyclerView
        monthYearText = mDataBinding.monthYearTV
        yearText = mDataBinding.yearTV
        fullMonthYearText = mDataBinding.monthYearTx
        // 2686 Set Current Date
        CalendarUtils.selectedDate = LocalDate.now()
        CalendarUtils.businessValidity = businessValidity
        // 2458 Method for  previousMonth
        previousMonthAction()
        // 2458 Method for  nextMonth
        nextMonthAction()
    }

    // 2685 Method for Setting the MonthDate
    private fun settingMonthDate() {
        val monthDate = calendarUtils.monthFromAndToDate()
        if (CalendarUtils.selectedDate?.monthValue!! <= cmonth && CalendarUtils.selectedDate?.year!! <= cyear) {
            monthDate.fromDate = ""
            monthDate.toDate = ""
            setCurrentMonth(monthDate)
        } else if (CalendarUtils.selectedDate?.year!! >= cyear) {
            setCurrentMonth(monthDate)
        }

    }

    // 2796 Method for setting current Month
    private fun setCurrentMonth(monthDate: CalendarUtils.MonthDates) {
        sharedViewModel.setCurrentMonthDate(
            monthDate.fromDate,
            monthDate.toDate,
            CalendarUtils.selectedDate!!.format(CalendarUtils.dateFormatter),
            CalendarUtils.selectedDate!!.monthValue,
            serviceCategoryId,
            serviceVendorOnboardingId,
            monthFromAndToDate()
        )
    }

    // 2686 Method for Setting the WeekDate
    private fun settingWeekDate() {
        val fromAndToDate = calendarUtils.fromAndToDate()
        val weeksOfMonth = calendarUtils.fetchWeekOfMonth()
        val cmonth = c.get(Calendar.MONTH)
        if (CalendarUtils.selectedDate?.monthValue!! < cmonth && CalendarUtils.selectedDate?.year!! <= cyear) {
            weeksOfMonth.clear()
            serviceDate.clear()
            setSelectedWeekSetter(weeksOfMonth, fromAndToDate)
        } else if (CalendarUtils.selectedDate?.monthValue!! >= cmonth && CalendarUtils.selectedDate?.year!! >= cyear) {
            setSelectedWeekSetter(weeksOfMonth, fromAndToDate)
        } else {
            setSelectedWeekSetter(weeksOfMonth, fromAndToDate)
        }
    }

    // 2622 Method for Calendar Format(Day,week,month) Picker
    private fun setCalendarFormat() {
        if (calendarFormat.isNullOrEmpty()) {
            calendarFormat = "Day"
            setMonthView(dayinWeek, daysPositon)
        }
        sharedViewModel.getCalendarFormat.observe(requireActivity(), {
            calendarFormat = it
            // 2458 Method For Setting Month View in Calendar
            setMonthView(dayinWeek, daysPositon)
        })
        selectedWeekEXPObserver()
    }

    // 2796 Method for week Observer
    private fun selectedWeekEXPObserver() {
        sharedViewModel.getExpCurrentWeek.observe(requireActivity(), {
            val currentDayFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
            val selectedDate: ArrayList<String> = ArrayList()
            it.forEach {
                selectedDate.add(it)
            }
            val selectedDateLocal: ArrayList<LocalDate> = ArrayList()
            selectedDate.forEach {
                selectedDateLocal.add(LocalDate.parse(it, currentDayFormatter))
            }
            val currentDay = LocalDate.parse(it.first(), currentDayFormatter)
            daysPositon?.clear()
            for (i in 0 until 7) {
                weekSelectedListAll!!.get(currentDay.plusDays(i.toLong()))
                    ?.let { it1 -> daysPositon?.add(it1) }
            }
            CalendarUtils.selectedDate = currentDay
            setMonthView(dayinWeek, daysPositon)
        })
    }

    // 2622
    // 2825 Method For Setting Month View in Calendar
    private fun setMonthView(dayinWeek: ArrayList<String>?, daysPositon: ArrayList<Int>?) {
        monthYearText?.text =
            CalendarUtils.selectedDate?.let { calendarUtils.monthYearFromDate(it) }
        fullMonthYearText?.text =
            CalendarUtils.selectedDate?.let { calendarUtils.monthYearFromDateFull(it) }
        yearText?.text = CalendarUtils.selectedDate?.let { calendarUtils.yearAndMonthFromDate(it) }
        CalendarUtils.selectedDate?.let { calendarUtils.monthYearFromDate(it) }
        val thisYear: Int = Calendar.getInstance().get(Calendar.YEAR)
        for (i in thisYear..2050) {
            resources.add(i.toString())
        }
        val daysInMonth: ArrayList<LocalDate>? = calendarUtils.daysInMonthArray()
        var calendarType = String()
        when (calendarFormat) {
            CalendarFormat.DAY -> {
                calendarType = CalendarFormat.DAY
            }
            CalendarFormat.WEEK -> {
                calendarType = CalendarFormat.WEEK
            }
            CalendarFormat.MONTH -> {
                calendarType = CalendarFormat.MONTH
            }
        }
        calendarAdapter =
            CalendarAdapter(
                daysInMonth, this, mDataBinding, calendarType, dayinWeek,
                daysPositon, serviceDate, businessValidity
            )
        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(context, 7)
        calendarRecyclerView?.layoutManager = layoutManager
        calendarRecyclerView?.adapter = calendarAdapter

    }

    // 2458 Method for  previousMonth
    private fun previousMonthAction() {
        mDataBinding.previousMonth.setOnClickListener {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate?.minusMonths(1)
            daysPositon = null
            setMonthView(dayinWeek, daysPositon)
            //  settingWeekDate()
            settingMonthDate()
            apiTokenValidationCalendar("EventDateApiPreviousActionAndNextMonth")
// 2803 CustomDialog Fragment
//            DeselectingDialogFragment.newInstance("sele")
//                .show(
//                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
//                    DeselectingDialogFragment.TAG
//                )
        }
    }

    // 2685
    // 2458 Method for  nextMonth
    private fun nextMonthAction() {
        mDataBinding.nextMonth.setOnClickListener {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate?.plusMonths(1)
            daysPositon = null
            setMonthView(dayinWeek, daysPositon)
            //  settingWeekDate()
            settingMonthDate()
            apiTokenValidationCalendar("EventDateApiPreviousActionAndNextMonth")
//            DeselectingDialogFragment.newInstance("Deselected")
//                .show(
//                    (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
//                    DeselectingDialogFragment.TAG
//                )
        }
    }

    // 2458 Callback Method for Clicked Date
    override fun onItemClick(
        position: Int,
        date: LocalDate?,
        dayinWeek: ArrayList<String>,
        daysPositon: ArrayList<Int>,
        selectedWeekDates: ArrayList<LocalDate>,
    ) {
        if (date != null) {
            CalendarUtils.selectedDate = date
            val weekList = ArrayList<String>()
            selectedWeekDates.forEach {
                weekList.add(it.format(CalendarUtils.dateFormatter))
            }
            sharedViewModel.setCurrentWeekDate(
                calendarUtils.fetchWeekOfMonth(),
                serviceCategoryId,
                serviceVendorOnboardingId,
                weekList, serviceDate
            )
            setMonthView(dayinWeek, daysPositon)
            sharedViewModel.setCurrentDate(
                CalendarUtils.selectedDate!!.format(CalendarUtils.dateFormatter),
                serviceCategoryId,
                serviceVendorOnboardingId,
                serviceDate,
                monthFromAndToDate()
            )
        }

    }

    // 2685 CallBack Mehod for WeekSelected Date Onclick
    override fun weekSelection(
        pos: ArrayList<Int>,
        selectedDate: LocalDate?,
        absoluteAdapterPosition: Int,
    ) {
        if (selectedDate != null) {
            CalendarUtils.selectedDate = selectedDate
            daysPositon = pos
            this.absoluteAdapterPosition = absoluteAdapterPosition
        }
        //setMonthView(dayinWeek, daysPositon)
    }

    override fun weekMapList(weekMapList: HashMap<LocalDate, Int>?) {
        Log.d("TAG", "weekMapList: $weekMapList")
        weekSelectedListAll = weekMapList
    }

    override fun onClickBusinessExpDate(valid: Boolean) {
        if (valid) {
            Toast.makeText(requireContext(),
                "Your Business registration valid to date is No longer available for the selected date",
                Toast.LENGTH_SHORT)
                .show()
        }else{
            Toast.makeText(requireContext(),
                "Your Last Business registration Date",
                Toast.LENGTH_SHORT)
                .show()
        }
    }

    // 2458 Setting IdToken, SpRegId And RollId
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
    }

    // 2458 CallBack for Clicked All Service Items
    override fun itemClick(msg: Int) {
        if (serviceList[msg].serviceName == "All Service") {
            val branchSpinner: ArrayList<String> = ArrayList()
            // branchSpinner.add(0, "Branches")
            serviceCategoryId = 0
            sharedViewModel.branches(
                mDataBinding,
                branchSpinner
            )
        }
        if (serviceList[msg].serviceName != "All Service") {
            serviceCategoryId = (serviceList[msg].serviceCategoryId)
            Log.d("TAG", "itemClick services: $serviceCategoryId")
            apiTokenValidationCalendar("Branches")
            branchListSpinner.clear()
            //apiTokenValidationCalendar("EventDateApiAllService")
        }
    }

    // 2458 CallBack for Clicked All Service Items
    override fun branchItemClick(
        serviceVendorOnboardingId: Int,
        name: String?,
        allServiceposition: Int?,
    ) {
        // Log.d(TAG, "branchItemClick: ")
        this.serviceVendorOnboardingId = branchListSpinner[serviceVendorOnboardingId].branchId
        Log.d("TAG", "branchItemClick: ${this.serviceVendorOnboardingId}")
        apiTokenValidationCalendar("EventDateApiBranches")
        //  settingWeekDate()
        settingMonthDate()

    }

    // 2458 Getting All Service
    private fun getAllServices() {
        sharedViewModel.getAllServices(idToken, spRegId)
            .observe(viewLifecycleOwner, { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
//                        serviceList.add(ServicesData("All Service", 0))
//                        branchListSpinner.add(BranchDatas("Branches", 0))
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
                        Toast.makeText(requireContext(), "Timeout", Toast.LENGTH_SHORT).show()
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
                        val branchTypeItems: List<DatasNew> = apiResponse.response.datas
                        // branchListSpinner.add(BranchDatas("Branches", 0))
                        for (i in branchTypeItems.indices) {
                            val branchName: String =
                                branchTypeItems[i].branchName
                            // 2458I want to show this when Selected
                            val branchId: Int = branchTypeItems[i].serviceVendorOnboardingId
                            branchListSpinner.add(BranchDatas(branchName, branchId))
                        }
                        val branchList: ArrayList<String> = ArrayList()
                        for (i in branchListSpinner.indices) {
                            val branchName: String =
                                branchListSpinner[i].branchName // 2458 I want to show this when Selected
                            branchList.add(branchName)
                        }
                        sharedViewModel.branches(
                            mDataBinding,
                            branchList
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

    // 2685 Method For Getting the Event Date and Counts from Api
    private fun eventDateAndCounts(serviceCategoryId: Int?, branchId: Int, idToken: String) {
        Log.d("TAG", "eventDateAndCounts in s: $serviceCategoryId")
        val serviceId: Int?
        var branchesId: Int? = null
        when {
            serviceCategoryId == 0 -> {
                serviceId = null
            }
            branchId == 0 -> {
                serviceId = serviceCategoryId
                branchesId = null
            }
            else -> {
                serviceId = serviceCategoryId
                Log.d("TAG", "eventDateAndCounts: $serviceId")
                branchesId = branchId
            }
        }
        val c: Calendar = Calendar.getInstance()
        val cmonth = c.get(Calendar.MONTH)
        val cyear = c.get(Calendar.YEAR)
        val monthDate = calendarUtils.monthFromAndToDate()
        //  2777 if condition for call the api for upcoming and current month
        if (CalendarUtils.selectedDate?.monthValue!! <= cmonth && CalendarUtils.selectedDate?.year!! <= cyear) {
            serviceDate.clear()
            sharedViewModel.setCurrentDate(
                CalendarUtils.selectedDate!!.format(CalendarUtils.dateFormatter),
                this.serviceCategoryId,
                this.serviceVendorOnboardingId,
                serviceDate, monthFromAndToDate()
            )
            val fromAndToDate = calendarUtils.fromAndToDate()
            val weeksOfMonth = calendarUtils.fetchWeekOfMonth()
            weeksOfMonth.clear()
            serviceDate.clear()
            setSelectedWeekSetter(weeksOfMonth, fromAndToDate)
        } else if (CalendarUtils.selectedDate?.year!! >= cyear) {
            sharedViewModel.getEventDates(
                idToken,
                spRegId,
                serviceId,
                branchesId,
                monthDate.fromDate,
                monthDate.toDate
            ).observe(viewLifecycleOwner, { apiresponse ->
                serviceDate.clear()
                when (apiresponse) {
                    is ApisResponse.Success -> {
                        Log.d(
                            "TAG",
                            "Calendar Event List : ${apiresponse.response.data.serviceDates}"
                        )
                        apiresponse.response.data.serviceDates.forEach {
                            val currentDayFormatter =
                                DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH)
                            var dateList = LocalDate.parse(it, currentDayFormatter)
                            if (dateList < LocalDate.now()) {
                                // previous Date
                            } else {
                                serviceDate.add(it)
                            }
                        }
                        sharedViewModel.setCurrentDate(
                            CalendarUtils.selectedDate!!.format(CalendarUtils.dateFormatter),
                            this.serviceCategoryId,
                            this.serviceVendorOnboardingId,
                            serviceDate, monthFromAndToDate()
                        )
                        // 2796
                        settingWeekDate()
                        setMonthView(dayinWeek, daysPositon)
                        Log.d("TAG", "Calendar Event:$serviceDate ")
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiresponse.exception}")
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Timeout", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    // 2686 - Method For AWS Token Validation
    private fun apiTokenValidationCalendar(caller: String) {
        if (idToken.isNotEmpty()) {
            tokens.checkTokenExpiry(
                requireActivity().applicationContext as SMFApp,
                caller, idToken
            )
        }
    }

    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Dispatchers.Main) {
            when (caller) {
                "EventDateApiPreviousActionAndNextMonth" -> {
                    eventDateAndCounts(serviceCategoryId, serviceVendorOnboardingId, idToken)
                }
                "EventDateApiAllService" -> {
                    Log.d("TAG", "tokenCallBack:$serviceCategoryId ")
                    eventDateAndCounts(
                        serviceCategoryId,
                        0,
                        idToken
                    )
                }
                "EventDateApiBranches" -> {
                    eventDateAndCounts(serviceCategoryId, serviceVendorOnboardingId, idToken)
                }
                "Branches" -> {
                    // 2458 Branch ApiCall
                    getBranches(idToken, serviceCategoryId)
                }
                "AllServices" -> {
                    // 2458 Method for All Service API call
                    getAllServices()
                }
                else -> {
                    Toast.makeText(requireContext(), "Timeout", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun monthFromAndToDate(): ArrayList<String> {
        dateList.clear()
        val fromDateMonth: LocalDate = CalendarUtils.selectedDate!!.withDayOfMonth(1)
        val toDateMonth: LocalDate =
            CalendarUtils.selectedDate!!.plusMonths(1).withDayOfMonth(1).minusDays(1)
        for (i in 0 until toDateMonth.dayOfMonth) {
            fromDateMonth.plusDays(i.toLong())
            if (fromDateMonth.plusDays(i.toLong()) >= LocalDate.now()) {
                dateList.add(fromDateMonth.plusDays(i.toLong()).format(CalendarUtils.dateFormatter))
            }
        }
        return dateList
    }
}