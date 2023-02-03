package com.smf.events.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.smf.events.BR
import com.smf.events.MainActivity
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentDashBoardBinding
import com.smf.events.helper.*
import com.smf.events.helper.SharedPreference.Companion.isDialogShown
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.actionandstatusdashboard.ActionsAndStatusFragment
import com.smf.events.ui.dashboard.adapter.MyEventsAdapter
import com.smf.events.ui.dashboard.model.BranchDatas
import com.smf.events.ui.dashboard.model.DatasNew
import com.smf.events.ui.dashboard.model.ServicesData
import com.smf.events.ui.notification.NotificationActivity
import com.smf.events.ui.schedulemanagement.ScheduleManagementActivity
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DashBoardFragment : BaseFragment<FragmentDashBoardBinding, DashBoardViewModel>(),
    DashBoardViewModel.CallBackInterface, Tokens.IdTokenCallBackInterface, View.OnTouchListener,
    ViewTreeObserver.OnScrollChangedListener, SwipeRefreshLayout.OnRefreshListener,
    NavigationView.OnNavigationItemSelectedListener {

    val TAG = DashBoardFragment::class.java.toString()
    private val args: DashBoardFragmentArgs by navArgs()
    var spRegId: Int = 0
    lateinit var idToken: String
    var roleId: Int = 0
    var firstName: String = ""
    var emailId: String = ""
    private lateinit var myEventsRecyclerView: RecyclerView
    lateinit var adapter: MyEventsAdapter
    var serviceList = ArrayList<ServicesData>()
    var serviceCategoryId: Int = 0
    var serviceVendorOnboardingId: Int = 0
    var branchListSpinner = ArrayList<BranchDatas>()
    var valueweget = 0
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var userId: String

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    override fun getViewModel(): DashBoardViewModel =
        ViewModelProvider(this, factory)[DashBoardViewModel::class.java]

    override fun getBindingVariable(): Int = BR.dashBoardViewModel

    override fun getContentView(): Int = R.layout.fragment_dash_board

    @Inject
    lateinit var tokens: Tokens
    private var pressedTime: Long = 0
    var p = 0
    private lateinit var internetDisposable: Disposable
    private lateinit var quoteBriefDisposable: Disposable
    private lateinit var quoteBriefDisposable1: Disposable

    companion object {
        var actionAndDetailsVisibility = true
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set Status bar
        (requireActivity() as MainActivity).setStatusBarColor()
        restrictBackButton()
        // Initialize Local Variables
        setIdTokenAndSpRegId()
    }

    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2904 Scroll Down Refresh Method
        mDataBinding?.refreshLayout?.setOnRefreshListener(this)
        // Initialize IdTokenCallBackInterface
        tokens.setCallBackInterface(this)
        // DashBoard ViewModel CallBackInterface
        getViewModel().setCallBackInterface(this)
        // 2888 SideNavBar SetUp
        sideNavBarInitialization()
        // 2839 Invisible the all the layout before api call
        widgetBefore()
        // Initialize MyEvent Recycler
        myEventsRecycler()
        // 2839 Method for arrow in event count recycler view
        arrowLeftAndRight()
        // 2458 CalendarIcon Onclick method
        onClickCalendar()
        // 3103 Notification Onclick method
        onClickNotification()
        if (args.fromEmailOtp == AppConstants.TRUE) {
            // Check token validation
            idTokenValidation()
        }
    }

    override fun onResume() {
        super.onResume()
        // Setting current navigation view item(Highlighting)
        navigationView.setCheckedItem(R.id.nav_dashboard)

        internetDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer DashBoard rx")
            if (isDialogShown.not()) {
                // Check token validation
                idTokenValidation()
            }
        }

        quoteBriefDisposable = RxBus.listen(RxEvent.QuoteBrief::class.java).subscribe {
            // Declaration for show action and details page
            actionAndDetailsVisibility = it.status
            if (actionAndDetailsVisibility.not()) {
                hideUpComingEvents()
                valueweget = it.bidReqId
            }
            Log.d(TAG, "actionAndDetailsVisibility  dash $actionAndDetailsVisibility")
        }

        quoteBriefDisposable1 = RxBus.listen(RxEvent.QuoteBrief1::class.java).subscribe {
            // Declaration for show action and details page
            actionAndDetailsVisibility = it.status
            if (actionAndDetailsVisibility) {
                showUpComingEvents()
            }
            Log.d(TAG, "actionAndDetailsVisibility  dash $actionAndDetailsVisibility")
        }
    }

    // 2888 - SideNav Initialization
    @SuppressLint("SetTextI18n")
    fun sideNavBarInitialization() {
        drawerLayout = mDataBinding!!.drawerLayout
        navigationView = mDataBinding!!.navView
        toolbar = mDataBinding?.toolBar!!
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        // Setting DashBoard Title
        mDataBinding?.welcomeText?.text = getString(R.string.welcome) + " " + firstName
        navigationView.bringToFront()
        val toggle = ActionBarDrawerToggle(
            requireActivity(),
            drawerLayout,
            toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle.setHomeAsUpIndicator(R.drawable.plus)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setHomeButtonEnabled(true)
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.menu, null)
        val bitmap = (drawable as (BitmapDrawable)).bitmap
        val sideNavIcon = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 55, 45, true))
        activity.supportActionBar?.setHomeAsUpIndicator(drawable)
        activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        navigationView.setNavigationItemSelectedListener(this)

        // 2888 - Side Navigation Header Inside Close Button ClickListener
        navigationView.getHeaderView(0).findViewById<ImageView>(R.id.side_nav_close_btn)
            .setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.user_name).text = firstName
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.user_email).text = emailId
    }

    // 2842 Method for Arrow Left and Right
    private fun arrowLeftAndRight() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mDataBinding?.rightBtn?.setOnClickListener {
            p = linearLayoutManager.findFirstVisibleItemPosition() - 1
            myEventsRecyclerView.nestedScrollBy(275, 0)
        }
        mDataBinding?.leftDtn?.setOnClickListener {
            p = linearLayoutManager.findLastVisibleItemPosition() + 1
            // myEventsRecyclerView.smoothScrollToPosition(p);
            myEventsRecyclerView.nestedScrollBy(-275, 0)
        }
    }

    private fun onClickCalendar() {
        mDataBinding?.calander?.setOnClickListener {
            // Declaration for show action and details page
            actionAndDetailsVisibility = true
            startActivity(Intent(this.requireContext(), ScheduleManagementActivity::class.java))
        }
    }

    private fun onClickNotification() {
        mDataBinding?.notificationBell?.setOnClickListener {
            // Declaration for show action and details page
            actionAndDetailsVisibility = true
            startActivity(Intent(this.requireContext(), NotificationActivity::class.java))
        }
    }

    private fun idTokenValidation() {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp,
            getString(R.string.event_type), idToken
        )
    }

    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Main) {
            when (caller) {
                getString(R.string.event_type) -> {
                    getAllServiceAndCounts(idToken)
                    getNotificationCount(idToken, userId)
                }
                getString(R.string.branch) -> getBranches(idToken, serviceCategoryId)
                else -> {}
            }
        }
    }

    private fun setAllService() {
        val allServiceList = ArrayList<String>().apply {
            Log.d(TAG, "setAllService: $serviceList")
            serviceList.forEach {
                this.add(it.serviceName)
            }
        }
        widgetAfter()
        //spinner view for all Services
        getViewModel().allServices(mDataBinding, allServiceList)
    }

    private fun myEventsRecycler() {
        myEventsRecyclerView = mDataBinding?.eventsRecyclerView!!
        adapter = MyEventsAdapter()
        myEventsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        myEventsRecyclerView.adapter = adapter
    }

    // Method for restrict user back button
    private fun restrictBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // 2888 - Condition For Check Side nav Open Status
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                if (pressedTime + 1500 > System.currentTimeMillis()) {
                    // 3103 - While Displaying ActionDetails fragment clicked the back key to close main activity
                    ApplicationUtils.fromNotification = false
                    requireActivity().finish()
                } else {
                    showToast(getString(R.string.Press_back_again_to_exit))
                }
                pressedTime = System.currentTimeMillis()
            }
        }
    }

    // All Service spinner view clicked
    override fun itemClick(position: Int) {
        if (serviceList[position].serviceName == getString(R.string.All_Service)) {
            var branchSpinner: ArrayList<String> = ArrayList()
            branchSpinner.add(0, getString(R.string.Branches))
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
        if (serviceList[position].serviceName != getString(R.string.All_Service)) {
            // Declaration for show action and details page
            actionAndDetailsVisibility = true
            showUpComingEvents()
            serviceCategoryId = (serviceList[position].serviceCategoryId)
            tokens.checkTokenExpiry(
                requireActivity().applicationContext as SMFApp,
                getString(R.string.branch), idToken
            )
            branchListSpinner.clear()
        }
    }

    // Branch spinner view clicked
    override fun branchItemClick(
        serviceVendorOnboardingId: Int,
        name: String?,
        allServiceposition: Int?,
    ) {
        this.serviceVendorOnboardingId = branchListSpinner[serviceVendorOnboardingId].branchId
        var branchesName = name
        if (branchListSpinner[serviceVendorOnboardingId].branchId == 0) {
            branchesName = getString(R.string.Branches)
        }
        try {
            // Declaration for show action and details page
            actionAndDetailsVisibility = true
            val serviceName = (serviceList[allServiceposition!!].serviceName)
            if (serviceName == getString(R.string.All_Service) && branchesName == getString(R.string.Branches)) {
                actionAndStatusFragmentMethod(
                    serviceCategoryId,
                    0
                )
            } else if (serviceName != getString(R.string.All_Service) && branchesName == getString(
                    R.string.Branches
                )
            ) {
                actionAndStatusFragmentMethod(
                    serviceCategoryId,
                    0
                )
            } else {
                actionAndStatusFragmentMethod(
                    serviceCategoryId,
                    branchListSpinner[serviceVendorOnboardingId].branchId
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "branchItemClick: $e")
        }
    }

    // Action And Status UI setUp
    fun actionAndStatusFragmentMethod(serviceCategoryId: Int, branchId: Int) {
        val args = Bundle().apply {
            putInt(AppConstants.SERVICE_CATEGORY_ID, serviceCategoryId)
            putInt(AppConstants.SERVICE_VENDOR_ON_BOARDING_ID, branchId)
            putBoolean("actionAndDetailsVisibility", actionAndDetailsVisibility)
        }
        val actionAndStatusFragment = ActionsAndStatusFragment().apply { arguments = args }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.action_and_status_layout, actionAndStatusFragment)
            .addToBackStack(null)
            .commit()
    }

    // Counts And AllService ApiCall
    fun getAllServiceAndCounts(idToken: String) {
        // Getting Service Provider Service Counts Status
        mDataBinding?.serviceCountLayout?.visibility = View.INVISIBLE
        getViewModel().getServiceCount(idToken, spRegId)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        val serviceList =
                            getViewModel().getServiceCountList(apiResponse.response.data)
                        adapter.refreshItems(serviceList)
                        mDataBinding?.serviceCountLayout?.visibility = View.VISIBLE
                        // 2842 Getting All Service
                        getAllServices(idToken)
                    }
                    is ApisResponse.CustomError -> {
                        Log.d(TAG, "check token result: ${apiResponse.message}")
                        showToastMessage(
                            apiResponse.message,
                            Snackbar.LENGTH_LONG,
                            AppConstants.PLAIN_SNACK_BAR
                        )
                        sharedPreference.putString(SharedPreference.ID_Token, "")
                        CoroutineScope(Main).launch {
                            // Navigate to SignInFragment
                            findNavController().navigate(DashBoardFragmentDirections.actionDashBoardFragmentToSignInFragment())
                            widgetAfter()
                        }
                    }
                    is ApisResponse.InternetError -> {
                        (requireActivity() as MainActivity).showInternetDialog(apiResponse.message)
                        widgetAfter()
                    }
                    else -> {}
                }
            })
    }

    // 2839 Invisible the all the layout before api call
    private fun widgetBefore() {
        mDataBinding?.progressBar?.visibility = View.VISIBLE
        mDataBinding?.calander?.visibility = View.INVISIBLE
        mDataBinding?.upcomingEvent?.visibility = View.INVISIBLE
        mDataBinding?.banner1?.visibility = View.INVISIBLE
        mDataBinding?.banner2?.visibility = View.INVISIBLE
        mDataBinding?.myEventsLayout?.visibility = View.INVISIBLE
        mDataBinding?.spinnerAction?.visibility = View.INVISIBLE
        mDataBinding?.serviceCountLayout?.visibility = View.INVISIBLE
        mDataBinding?.notificationBell?.visibility = View.INVISIBLE
    }

    // 2839 After Api call Done
    private fun widgetAfter() {
        mDataBinding?.progressBar?.visibility = View.GONE
        mDataBinding?.calander?.visibility = View.VISIBLE
        mDataBinding?.upcomingEvent?.visibility = View.VISIBLE
        mDataBinding?.banner1?.visibility = View.VISIBLE
        mDataBinding?.banner2?.visibility = View.VISIBLE
        mDataBinding?.myEventsLayout?.visibility = View.VISIBLE
        mDataBinding?.spinnerAction?.visibility = View.VISIBLE
        mDataBinding?.serviceCountLayout?.visibility = View.VISIBLE
        mDataBinding?.notificationBell?.visibility = View.VISIBLE
    }

    // 2842 Getting All Service
    fun getAllServices(idToken: String) {
        getViewModel().getAllServices(idToken, spRegId)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        serviceList.clear()
                        serviceList.add(ServicesData(getString(R.string.All_Service), 0))
                        branchListSpinner.add(BranchDatas(getString(R.string.Branches), 0))
                        apiResponse.response.data.forEach {
                            serviceList.add(ServicesData(it.serviceName, it.serviceCategoryId))
                        }
                        setAllService()
                    }
                    is ApisResponse.CustomError -> {
                        Log.d(TAG, "check token result: ${apiResponse.message}")
                        showToastMessage(
                            apiResponse.message,
                            Snackbar.LENGTH_LONG,
                            AppConstants.PLAIN_SNACK_BAR
                        )
                        widgetAfter()
                    }
                    is ApisResponse.InternetError -> {
                        (requireActivity() as MainActivity).showInternetDialog(apiResponse.message)
                        widgetAfter()
                    }
                    else -> {}
                }
            })
    }

    // Branch ApiCall
    fun getBranches(idToken: String, serviceCategoryId: Int) {
        getViewModel().getServicesBranches(idToken, spRegId, serviceCategoryId)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        val branchTypeItems: List<DatasNew> = apiResponse.response.datas
                        branchListSpinner.add(BranchDatas(getString(R.string.Branches), 0))
                        branchListSpinner.apply {
                            for (i in branchTypeItems.indices) {
                                val branchName: String = branchTypeItems[i].branchName
                                // I want to show this when Selected
                                val branchId: Int = branchTypeItems[i].serviceVendorOnboardingId
                                this.add(BranchDatas(branchName, branchId))
                            }
                        }
                        val branchList = ArrayList<String>().apply {
                            for (i in branchListSpinner.indices) {
                                // I want to show this when Selected
                                this.add(branchListSpinner[i].branchName)
                            }
                        }
                        getViewModel().branches(
                            mDataBinding,
                            branchList,
                            this.idToken,
                            spRegId,
                            serviceCategoryId,
                            0
                        )
                    }
                    is ApisResponse.CustomError -> {
                        Log.d(TAG, "check token result: ${apiResponse.message}")
                        showToastMessage(
                            apiResponse.message,
                            Snackbar.LENGTH_LONG,
                            AppConstants.PLAIN_SNACK_BAR
                        )
                        widgetAfter()
                    }
                    is ApisResponse.InternetError -> {
                        (requireActivity() as MainActivity).showInternetDialog(apiResponse.message)
                        widgetAfter()
                    }
                    else -> {
                    }
                }
            })
    }

    // Setting IdToken, SpRegId And RollId
    private fun setIdTokenAndSpRegId() {
        userId = "${sharedPreference.getString(SharedPreference.USER_ID)}"
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        roleId = sharedPreference.getInt(SharedPreference.ROLE_ID)
        firstName = sharedPreference.getString(SharedPreference.FIRST_NAME).toString()
        emailId = sharedPreference.getString(SharedPreference.EMAIL_ID).toString()
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return valueweget == 1
    }

    override fun onScrollChanged() {
//        var view: View? = mDataBinding?.nestedscroll?.childCount?.let {
//            mDataBinding?.nestedscroll?.getChildAt(it.minus(1) )
//        }
//        var topDector=mDataBinding?.nestedscroll?.scrollY
//        var bottomDetector= mDataBinding?.nestedscroll?.height?.let { view?.bottom?.minus(it.plus(
//            mDataBinding?.nestedscroll?.scaleY!!)) }
    }

    // 2904 Scroll Refresh Method
    override fun onRefresh() {
        // 3103 Displaying ActionDetails refresh the dashboard redirect to ActionAndStatus Page
        ApplicationUtils.fromNotification = false
        serviceList.clear()
        // Declaration for show action and details page
        actionAndDetailsVisibility = true
        idTokenValidation()
        mDataBinding?.refreshLayout?.isRefreshing = false
    }

    // 2888 - NavigationView ItemSelected Override Method
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                // 3103 Displaying ActionDetails refresh the dashboard redirect to ActionAndStatus Page
                ApplicationUtils.fromNotification = false
                serviceList.clear()
                // Declaration for show action and details page
                actionAndDetailsVisibility = true
                idTokenValidation()
            }
            R.id.nav_availability -> {
                val intent = Intent(this.requireContext(), ScheduleManagementActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                logOut()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logOut() {
        widgetBefore()
        Amplify.Auth.signOut(
            AuthSignOutOptions.builder().globalSignOut(true).build(),
            {
                sharedPreference.putString(SharedPreference.ID_Token, "")
                GlobalScope.launch(Main) {
                    // Navigate to SignInFragment
                    findNavController().navigate(DashBoardFragmentDirections.actionDashBoardFragmentToSignInFragment())
                    widgetAfter()
                }
            },
            {
                Log.e("AuthQuickstart", "Sign out failed", it)
                logOutError()
            }
        )
    }

    private fun logOutError() {
        GlobalScope.launch(Main) {
            widgetAfter()
        }
    }

    private fun showUpComingEvents() {
        mDataBinding?.upcomingEvent?.visibility = View.VISIBLE
        mDataBinding?.banner1?.visibility = View.VISIBLE
        mDataBinding?.banner2?.visibility = View.VISIBLE
        mDataBinding?.nestedScroll?.visibility = View.VISIBLE
    }

    private fun hideUpComingEvents() {
        mDataBinding?.upcomingEvent?.visibility = View.GONE
        mDataBinding?.banner1?.visibility = View.GONE
        mDataBinding?.banner2?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onStop: Destroy called dash onpause")
        if (internetDisposable.isDisposed.not()) internetDisposable.dispose()
        if (quoteBriefDisposable.isDisposed.not()) quoteBriefDisposable.dispose()
        if (quoteBriefDisposable1.isDisposed.not()) quoteBriefDisposable1.dispose()
    }

    private fun getNotificationCount(
        idToken: String,
        userId: String
    ) {
        getViewModel().getNotificationCount(idToken, userId)
            .observe(this, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        // Active notification count
                        notificationCount(apiResponse.response.data.activeCounts)
                    }
                    is ApisResponse.CustomError -> {
                        Log.d(TAG, "check token result: ${apiResponse.message}")
                        showToastMessage(
                            apiResponse.message,
                            Snackbar.LENGTH_LONG,
                            AppConstants.PLAIN_SNACK_BAR
                        )
                        widgetAfter()
                    }
                    else -> {}
                }
            })
    }

    // 3218 Notification bell icon counts
    private fun notificationCount(activeCounts: Int) {
        if (activeCounts != 0) {
            mDataBinding?.notificationBell?.setImageResource(R.drawable.notification)
            if (activeCounts <= 9) {
                mDataBinding?.notificationCountSingle?.text = activeCounts.toString()
                mDataBinding?.notificationCount?.visibility = View.INVISIBLE
                mDataBinding?.notificationPlus?.visibility = View.INVISIBLE
            } else {
                mDataBinding?.notificationCount?.text = getString(R.string.nine)
                mDataBinding?.notificationPlus?.text = getString(R.string.plus_symbol)
                mDataBinding?.notificationCountSingle?.visibility = View.INVISIBLE
            }
        } else {
            mDataBinding?.notificationCountSingle?.visibility = View.INVISIBLE
            mDataBinding?.notificationCount?.visibility = View.INVISIBLE
            mDataBinding?.notificationPlus?.visibility = View.INVISIBLE
        }
    }
}