package com.smf.events.ui.actionandstatusdashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentActionsAndStatusBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.actionandstatusdashboard.adapter.ActionsAdapter
import com.smf.events.ui.actiondetails.ActionDetailsFragment
import com.smf.events.ui.dashboard.adapter.StatusAdaptor
import com.smf.events.ui.dashboard.model.ActionAndStatusCount
import com.smf.events.ui.dashboard.model.MyEvents
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActionsAndStatusFragment :
    BaseFragment<FragmentActionsAndStatusBinding, ActionsAndStatusViewModel>(),
    ActionsAdapter.OnActionCardClickListener, Tokens.IdTokenCallBackInterface {

    private lateinit var myActionRecyclerView: RecyclerView
    lateinit var actionAdapter: ActionsAdapter
    private lateinit var myStatusRecyclerView: RecyclerView
    lateinit var statusAdapter: StatusAdaptor
    lateinit var actionAndStatusData: ActionAndStatusCount
    var spRegId: Int = 0
    lateinit var idToken: String
    var roleId: Int = 0
    var serviceCategoryId: Int? = null
    var serviceVendorOnboardingId: Int? = null

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var tokens: Tokens

    override fun getViewModel(): ActionsAndStatusViewModel =
        ViewModelProvider(this, factory).get(ActionsAndStatusViewModel::class.java)

    override fun getBindingVariable(): Int = BR.actionsAndStatusViewModel

    override fun getContentView(): Int = R.layout.fragment_actions_and_status

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Local Variables
        setIdTokenAndSpRegId()
        // Set Category Id And ServiceOnBoarding Id
        serviceCategoryIdAndServiceOnBoardingIdSetup()
    }

    override fun onStart() {
        super.onStart()
        // Token Class CallBack Initialization
        tokens.setCallBackInterface(this)
        // Action And Status Api Call
        apiTokenValidationActionAndStatus()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Initializing actions recyclerview
        myActionRecyclerView = mDataBinding?.actionsRecyclerview!!
        //Initializing status recyclerview
        myStatusRecyclerView = mDataBinding?.statusRecyclerview!!
        //Actions  Recycler view
        myActionsStatusRecycler()
        //Status Recycler view
        myStatusRecycler()
    }

    // Method For ActionsStatusRecyclerView SetUp
    private fun myActionsStatusRecycler() {
        actionAdapter = ActionsAdapter(requireContext())
        myActionRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        myActionRecyclerView.adapter = actionAdapter
        actionAdapter.setOnClickListener(this)
    }

    // Method For StatusRecyclerView SetUp
    private fun myStatusRecycler() {
        statusAdapter = StatusAdaptor()
        myStatusRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        myStatusRecyclerView.adapter = statusAdapter
    }

    // Action Card Click Listener Interface Method
    override fun actionCardClick(myEvents: MyEvents) {
        RxBus.publish(RxEvent.QuoteBrief(1))
        //
        when (myEvents.titleText) {
            AppConstants.NEW_REQUEST -> {
                goToActionDetailsFragment(AppConstants.BID_REQUESTED)
            }
            AppConstants.PENDING_QUOTE -> {
                goToActionDetailsFragment(AppConstants.PENDING_FOR_QUOTE)
            }
            // 2885 Bid Rejected flow
            AppConstants.REJECTED_BID -> {
                goToActionDetailsFragment(AppConstants.BID_REJECTED)
            }
            AppConstants.SUBMITTED_BID -> {
                goToActionDetailsFragment(AppConstants.BID_SUBMITTED)
            }
            // 2884 for won Bid flow
            AppConstants.BID_WON -> {
                goToActionDetailsFragment(AppConstants.WON_BID)
            }
            // 2885 Lost Bid flow
            AppConstants.BID_LOST -> {
                goToActionDetailsFragment(AppConstants.LOST_BID)
            }
            else -> {
                Log.d("TAG", "newRequestApiCallsample :else block")
            }
        }
    }

    // Method For AWS Token Validation Action And Status
    private fun apiTokenValidationActionAndStatus() {
        if (idToken.isNotEmpty()) {
            tokens.checkTokenExpiry(
                requireActivity().applicationContext as SMFApp,
                "actionAndStatus", idToken
            )
        }
    }

    // 2560 Method For ApiCall For Action And Status Counts
    private fun actionAndStatusApiCall(idToken: String) {
        getViewModel().getActionAndStatus(
            idToken,
            spRegId,
            serviceCategoryId,
            serviceVendorOnboardingId
        )
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        // 2560 changed the Api call variable names
                        actionAndStatusData = ActionAndStatusCount(
                            apiResponse.response.actionandStatus.bidRequestedCount,
                            apiResponse.response.actionandStatus.bidSubmittedCount,
                            apiResponse.response.actionandStatus.bidRejectedCount,
                            apiResponse.response.actionandStatus.pendingForQuoteCount,
                            apiResponse.response.actionandStatus.wonBidCount,
                            apiResponse.response.actionandStatus.lostBidCount,
                            apiResponse.response.actionandStatus.bidTimedOutCount,
                            apiResponse.response.actionandStatus.serviceDoneCount,
                            apiResponse.response.actionandStatus.statusCount,
                            apiResponse.response.actionandStatus.actionCount
                        )
                        recyclerViewListUpdate()
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
    }

    // Method For Update Action And Status Count To RecyclerView List
    private fun recyclerViewListUpdate() {
        var listActions1 = getViewModel().getActionsList(actionAndStatusData)
        actionAdapter.refreshItems(listActions1)
        val listStatus = getViewModel().getStatusList(actionAndStatusData)
        statusAdapter.refreshItems(listStatus)
        mDataBinding?.txPendtingitems?.text =
            "${actionAndStatusData.actionCount} PendingItems"
        mDataBinding?.txPendingstatus?.text =
            "${actionAndStatusData.statusCount} Status"
    }

    // Method For Set ServiceCategoryId And ServiceOnboardId For Api Call
    private fun serviceCategoryIdAndServiceOnBoardingIdSetup() {
        val args = arguments
        if (args?.getInt("serviceCategoryId") == 0) {
            if (args.getInt("serviceVendorOnboardingId") == 0) {
                serviceCategoryId = null
                serviceVendorOnboardingId = null
            }
        } else if (args?.getInt("serviceCategoryId") != 0 && args?.getInt("serviceVendorOnboardingId") == 0) {
            serviceCategoryId = args.getInt("serviceCategoryId")
            serviceVendorOnboardingId = null
        } else {
            serviceCategoryId = args?.getInt("serviceCategoryId")
            serviceVendorOnboardingId = args?.getInt("serviceVendorOnboardingId")
        }
    }

    // Callback From Token Class
    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Dispatchers.Main) {
            when (caller) {
                "actionAndStatus" -> actionAndStatusApiCall(idToken)
            }
        }
    }

    // Method For Calling ActionDetailsFragment With Action Details
    private fun goToActionDetailsFragment(bidStatus: String) {
        val args = Bundle()
        args.putString("bidStatus", bidStatus)
        serviceCategoryId?.let { args.putInt("serviceCategoryId", it) }
        serviceVendorOnboardingId?.let { args.putInt("serviceVendorOnboardingId", it) }
        val actionDetailsFragment = ActionDetailsFragment()
        actionDetailsFragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.action_and_status_layout, actionDetailsFragment)
            .addToBackStack(ActionsAndStatusFragment::class.java.name)
            .commit()
    }

    // Method For Set IdToken And SpRegId From SharedPreferences
    private fun setIdTokenAndSpRegId() {
        spRegId = sharedPreference.getInt(SharedPreference.SP_REG_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
        roleId = sharedPreference.getInt(SharedPreference.ROLE_ID)
    }

}