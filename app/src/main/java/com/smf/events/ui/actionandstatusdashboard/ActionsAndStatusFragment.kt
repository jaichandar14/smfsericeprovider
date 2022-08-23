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
import com.smf.events.helper.*
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.actionandstatusdashboard.adapter.ActionsAdapter
import com.smf.events.ui.actiondetails.ActionDetailsFragment
import com.smf.events.ui.dashboard.adapter.StatusAdaptor
import com.smf.events.ui.dashboard.model.ActionAndStatusCount
import com.smf.events.ui.dashboard.model.MyEvents
import com.smf.events.ui.notification.model.NotificationParams
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActionsAndStatusFragment :
    BaseFragment<FragmentActionsAndStatusBinding, ActionsAndStatusViewModel>(),
    ActionsAdapter.OnActionCardClickListener, StatusAdaptor.OnActionCardClickListener,
    Tokens.IdTokenCallBackInterface,
    ActionsAndStatusViewModel.CallBackInterface {

    var TAG = "ActionsAndStatusFragment"
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
    private lateinit var dialogDisposable: Disposable
    private lateinit var internetErrorDialog: InternetErrorDialog

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
        // Token Class CallBack Initialization
        tokens.setCallBackInterface(this)

        // 3103
        val notificationParams: NotificationParams? =
            requireActivity().intent.getParcelableExtra(AppConstants.NOTIFICATION_PARAMS)

        if (ApplicationUtils.fromNotification) {
            Log.d(TAG, "onCreate: called if ${notificationParams?.bidStatus}")
            notificationParams?.bidStatus?.let { goToActionDetailsFragment(it) }
        } else {
            Log.d(TAG, "onCreate: called else")
            apiTokenValidationActionAndStatus()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgress()
        //Initializing actions recyclerview
        myActionRecyclerView = mDataBinding?.actionsRecyclerview!!
        //Initializing status recyclerview
        myStatusRecyclerView = mDataBinding?.statusRecyclerview!!
        // Internet Error Dialog Initialization
        internetErrorDialog = InternetErrorDialog.newInstance()
        //Actions  Recycler view
        myActionsStatusRecycler()
        //Status Recycler view
        myStatusRecycler()

        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer ActionAndStatus")
            internetErrorDialog.dismissDialog()
        }
    }

    private fun showProgress() {
        mDataBinding?.progressBar?.visibility = View.VISIBLE
        mDataBinding?.txActions?.visibility = View.GONE
        mDataBinding?.txStatus?.visibility = View.GONE
        mDataBinding?.txPendtingitems?.visibility = View.GONE
        mDataBinding?.txPendingstatus?.visibility = View.GONE
        mDataBinding?.actionsRecyclerview?.visibility = View.GONE
        mDataBinding?.statusRecyclerview?.visibility = View.GONE
    }

    private fun hideProgress() {
        mDataBinding?.progressBar?.visibility = View.GONE
        mDataBinding?.txActions?.visibility = View.VISIBLE
        mDataBinding?.txStatus?.visibility = View.VISIBLE
        mDataBinding?.txPendtingitems?.visibility = View.VISIBLE
        mDataBinding?.txPendingstatus?.visibility = View.VISIBLE
        mDataBinding?.actionsRecyclerview?.visibility = View.VISIBLE
        mDataBinding?.statusRecyclerview?.visibility = View.VISIBLE
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
        statusAdapter.setOnClickListener(this)
    }

    // Action Card Click Listener Interface Method
    override fun actionCardClick(myEvents: MyEvents) {
        if (internetErrorDialog.checkInternetAvailable(requireContext())) {
            RxBus.publish(RxEvent.QuoteBrief(1))
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
                AppConstants.QUOTE_SENT -> {
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
                AppConstants.SERVICE_PROGRESS -> {
                    goToActionDetailsFragment(AppConstants.SERVICE_IN_PROGRESS)
                }
                AppConstants.REQUEST_CLOSED -> {
                    goToActionDetailsFragment(AppConstants.SERVICE_DONE)
                }
                AppConstants.TIMED_OUT_BID -> {
                    goToActionDetailsFragment(AppConstants.BID_TIMED_OUT)
                }
                else -> {
                    Log.d(TAG, "newRequestApiCallsample :else block")
                }
            }
        }
    }

    // Method For AWS Token Validation Action And Status
    private fun apiTokenValidationActionAndStatus() {
        if (idToken.isNotEmpty()) {
            tokens.checkTokenExpiry(
                requireActivity().applicationContext as SMFApp,
                getString(R.string.actionAndStatus), idToken
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
                            apiResponse.response.actionandStatus.actionCount,
                            apiResponse.response.actionandStatus.serviceInProgressCount
                        )
                        recyclerViewListUpdate()
                    }
                    is ApisResponse.Error -> {
                        Log.d(TAG, "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
    }

    // Method For Update Action And Status Count To RecyclerView List
    private fun recyclerViewListUpdate() {
        hideProgress()
        var listActions1 = getViewModel().getActionsList(actionAndStatusData)
        actionAdapter.refreshItems(listActions1)
        val listStatus = getViewModel().getStatusList(actionAndStatusData)
        statusAdapter.refreshItems(listStatus)
        // 2891 fetching all the active and inactive count
        var activeCounts =
            actionAndStatusData.bidRequestedCount + actionAndStatusData.pendingForQuoteCount +
                    actionAndStatusData.bidSubmittedCount + actionAndStatusData.wonBidCount + actionAndStatusData.serviceInProgressCount
        var inActiveCounts =
            actionAndStatusData.serviceDoneCount + actionAndStatusData.bidRejectedCount +
                    actionAndStatusData.bidTimedOutCount + actionAndStatusData.lostBidCount
        mDataBinding?.txPendtingitems?.text =
            "$activeCounts ${getString(R.string.active_status)}"
        mDataBinding?.txPendingstatus?.text =
            "$inActiveCounts ${getString(R.string.inactive_status)}"
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
                getString(R.string.actionAndStatus) -> actionAndStatusApiCall(idToken)
            }
        }
    }

    // Method For Calling ActionDetailsFragment With Action Details
    private fun goToActionDetailsFragment(bidStatus: String) {
        val args = Bundle()
        args.putString(getString(R.string.bidStatus), bidStatus)
        serviceCategoryId?.let { args.putInt(AppConstants.SERVICE_CATEGORY_ID, it) }
        serviceVendorOnboardingId?.let {
            args.putInt(
                AppConstants.SERVICE_VENDOR_ON_BOARDING_ID,
                it
            )
        }
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

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called ActionAndStatus")
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }

    override fun internetError(exception: String) {
        SharedPreference.isInternetConnected = false
        internetErrorDialog.checkInternetAvailable(requireContext())
        hideProgress()
    }

}