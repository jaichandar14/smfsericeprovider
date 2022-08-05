package com.smf.events.ui.actionandstatusdashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smf.events.base.BaseViewModel
import com.smf.events.helper.AppConstants
import com.smf.events.ui.dashboard.model.ActionAndStatusCount
import com.smf.events.ui.dashboard.model.MyEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class ActionsAndStatusViewModel @Inject constructor(
    private val actionsAndStatusRepository: ActionsAndStatusRepository,
    application: Application,
) : BaseViewModel(application) {

    var newRequestCount: Int = 0

    // 2560 Prepare Action List Values
    fun getActionsList(actionAndStatusData: ActionAndStatusCount?): ArrayList<MyEvents> {
        var list = ArrayList<MyEvents>()
        list.add(
            MyEvents(
                actionAndStatusData?.bidRequestedCount.toString(),
                AppConstants.NEW_REQUEST
            )
        )
        newRequestCount = actionAndStatusData!!.bidRequestedCount
        list.add(
            MyEvents(
                actionAndStatusData.pendingForQuoteCount.toString(),
                AppConstants.PENDING_QUOTE
            )
        )
        list.add(
            MyEvents(
                actionAndStatusData.bidSubmittedCount.toString(),
                AppConstants.QUOTE_SENT
            )
        )
//        list.add(MyEvents(actionAndStatusData.bidRejectedCount.toString(),
//            AppConstants.REJECTED_BID))
        list.add(MyEvents(actionAndStatusData.wonBidCount.toString(), AppConstants.BID_WON))
        list.add(
            MyEvents(
                actionAndStatusData.serviceInProgressCount.toString(),
                AppConstants.SERVICE_PROGRESS
            )
        )
        // list.add(MyEvents(actionAndStatusData.lostBidCount.toString(), AppConstants.BID_LOST))
        list.add(MyEvents(AppConstants.ZERO, AppConstants.PENDING_FOR_REVIEW))
        return list
    }

    // 2560 Prepare Status List Values
    fun getStatusList(actionAndStatusData: ActionAndStatusCount): ArrayList<MyEvents> {
        var list = ArrayList<MyEvents>()
        list.add(
            MyEvents(
                actionAndStatusData.serviceDoneCount.toString(),
                AppConstants.REQUEST_CLOSED
            )
        )
        list.add(
            MyEvents(
                actionAndStatusData.bidRejectedCount.toString(),
                AppConstants.REJECTED_BID
            )
        )
        list.add(
            MyEvents(
                actionAndStatusData.bidTimedOutCount.toString(),
                AppConstants.TIMED_OUT_BID
            )
        )
        list.add(MyEvents(actionAndStatusData.lostBidCount.toString(), AppConstants.BID_LOST))
        //    list.add(MyEvents(AppConstants.ZERO, AppConstants.PENDING_FOR_REVIEW))
        return list
    }

    // Method For Getting Action And Status
    fun getActionAndStatus(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int?,
        serviceVendorOnboardingId: Int?,
    ) = liveData(Dispatchers.IO) {
        try {
            emit(
                actionsAndStatusRepository.getActionAndStatus(
                    idToken,
                    spRegId,
                    serviceCategoryId,
                    serviceVendorOnboardingId
                )
            )
        } catch (e: Exception) {
            Log.d("TAG", "getActionAndStatus: $e")
            when (e) {
                is UnknownHostException -> {
                    Log.d("TAG", "getActionAndStatus: UnknownHostException $e")
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
                is ConnectException ->{
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
                else -> {}
            }
        }
    }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBack Interface
    interface CallBackInterface {
        fun internetError(exception: String)
    }
}