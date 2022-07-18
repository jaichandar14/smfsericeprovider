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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.google.android.material.navigation.NavigationView
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentDashBoardBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.actionandstatusdashboard.ActionsAndStatusFragment
import com.smf.events.ui.dashboard.adapter.MyEventsAdapter
import com.smf.events.ui.dashboard.model.BranchDatas
import com.smf.events.ui.dashboard.model.DatasNew
import com.smf.events.ui.dashboard.model.ServicesData
import com.smf.events.ui.schedulemanagement.ScheduleManagementActivity
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
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

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    override fun getViewModel(): DashBoardViewModel =
        ViewModelProvider(this, factory).get(DashBoardViewModel::class.java)

    override fun getBindingVariable(): Int = BR.dashBoardViewModel

    override fun getContentView(): Int = R.layout.fragment_dash_board

    companion object {
        fun newInstance(): DashBoardFragment {
            return DashBoardFragment()
        }
    }

    @Inject
    lateinit var tokens: Tokens
    private var pressedTime: Long = 0
    var p = 0
    private lateinit var dialogDisposable: Disposable
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restrictBackButton()
        // Initialize Local Variables
        setIdTokenAndSpRegId()
    }

    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 2888 SideNavBar SetUp
        sideNavBarInitialization()
        // 2839 Invisible the all the layout before api call
        widgetBefore()
        // Initialize IdTokenCallBackInterface
        tokens.setCallBackInterface(this)
        // DashBoard ViewModel CallBackInterface
        getViewModel().setCallBackInterface(this)
        // Initialize MyEvent Recycler
        myEventsRecycler()
        // Id Token Validation
        idTokenValidation()
        // 2458 CalendarIcon Onclick method
        onClickCalendar()
        // 2839 Method for arrow in event count recycler view
        arrowLeftAndRight()

        dialogDisposable = RxBus.listen(RxEvent.QuoteBrief::class.java).subscribe {
            Log.d("TAG", "onViewCreated listener: $it")
            mDataBinding?.upcomingEvent?.visibility = View.GONE
            mDataBinding?.banner1?.visibility = View.GONE
            mDataBinding?.banner2?.visibility = View.GONE
            valueweget = it.bidReqId
            //  mDataBinding?.loop1?.visibility=View.VISIBLE
            // mDataBinding?.nestedScroll?.visibility=View.GONE
        }
        dialogDisposable = RxBus.listen(RxEvent.QuoteBrief1::class.java).subscribe {
            Log.d("TAG", "onViewCreated listener: $it")
            mDataBinding?.upcomingEvent?.visibility = View.VISIBLE
            mDataBinding?.banner1?.visibility = View.VISIBLE
            mDataBinding?.banner2?.visibility = View.VISIBLE
            mDataBinding?.nestedScroll?.visibility = View.VISIBLE

        }
        // 2904 Scroll Down Refresh Method
        mDataBinding?.refreshLayout?.setOnRefreshListener(this)

    }

    // 2888 - SideNav Initialization
    @SuppressLint("SetTextI18n")
    private fun sideNavBarInitialization() {
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
        navigationView.setCheckedItem(R.id.nav_dashboard)

        // 2888 - Side Navigation Header Inside Close Button ClickListener
        navigationView.getHeaderView(0).findViewById<ImageView>(R.id.side_nav_close_btn)
            .setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.user_name).text = firstName
        navigationView.getHeaderView(0).findViewById<TextView>(R.id.user_email).text = emailId


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
            val intent = Intent(this.requireContext(), ScheduleManagementActivity::class.java)
            startActivity(intent)
        }
    }

    private fun idTokenValidation() {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp,
            "event_type", idToken
        )
    }

    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Main) {
            when (caller) {
                "event_type" -> getAllServiceAndCounts(idToken)
                "branches" -> getBranches(idToken, serviceCategoryId)
                else -> {
                }
            }
        }
    }

    private fun setAllService() {
        val allServiceList: ArrayList<String> = ArrayList()
        serviceList.forEach {
            allServiceList.add(it.serviceName)
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
            tokens.checkTokenExpiry(
                requireActivity().applicationContext as SMFApp,
                "branches", idToken
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
            branchesName = "Branches"
        }
        var serviceName = (serviceList[allServiceposition!!].serviceName)
        if (serviceName == "All Service" && branchesName == "Branches") {
            actionAndStatusFragmentMethod(
                serviceCategoryId,
                0
            )
        } else if (serviceName != "All Service" && branchesName == "Branches") {
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
    }

    // Action And Status UI setUp
    private fun actionAndStatusFragmentMethod(serviceCategoryId: Int, branchId: Int) {
        var args = Bundle()
        args.putInt("serviceCategoryId", serviceCategoryId)
        args.putInt("serviceVendorOnboardingId", branchId)
        var actionAndStatusFragment = ActionsAndStatusFragment()
        actionAndStatusFragment.arguments = args

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(com.smf.events.R.id.action_and_status_layout, actionAndStatusFragment)
            .setReorderingAllowed(true)
            .commit()
    }

    // Counts And AllService ApiCall
    private fun getAllServiceAndCounts(idToken: String) {
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

                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                        sharedPreference.putString(SharedPreference.ID_Token, "")
                        GlobalScope.launch(Main) {
                            // Navigate to SignInFragment
                            findNavController().navigate(DashBoardFragmentDirections.actionDashBoardFragmentToSignInFragment())
                            widgetAfter()
                        }
                    }
                    else -> {
                    }
                }
            })
        // 2842 Getting All Service
        getAllServices(idToken)
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
    }

    // 2842 Getting All Service
    private fun getAllServices(idToken: String) {
        getViewModel().getAllServices(idToken, spRegId)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        serviceList.add(ServicesData("All Service", 0))
                        branchListSpinner.add(BranchDatas("Branches", 0))
                        apiResponse.response.data.forEach {
                            serviceList.add(ServicesData(it.serviceName, it.serviceCategoryId))
                        }
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

    // Branch ApiCall
    private fun getBranches(idToken: String, serviceCategoryId: Int) {

        getViewModel().getServicesBranches(idToken, spRegId, serviceCategoryId)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        var branchTypeItems: List<DatasNew> = apiResponse.response.datas
                        branchListSpinner.add(BranchDatas("Branches", 0))
                        for (i in branchTypeItems.indices) {
                            val branchName: String =
                                branchTypeItems[i].branchName
                            // I want to show this when Selected
                            val branchId: Int = branchTypeItems[i].serviceVendorOnboardingId
                            branchListSpinner.add(BranchDatas(branchName, branchId))
                        }
                        var branchList: ArrayList<String> = ArrayList()
                        for (i in branchListSpinner.indices) {
                            val branchName: String =
                                branchListSpinner[i].branchName // I want to show this when Selected
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

    // Setting IdToken, SpRegId And RollId
    private fun setIdTokenAndSpRegId() {
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
        idTokenValidation()
        actionAndStatusFragmentMethod(serviceCategoryId, serviceVendorOnboardingId)
        mDataBinding?.refreshLayout?.isRefreshing = false
    }

    // 2888 - NavigationView ItemSelected Override Method
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                drawerLayout.closeDrawer(GravityCompat.START)
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
            { Log.e("AuthQuickstart", "Sign out failed", it) }
        )
    }

}