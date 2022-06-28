package com.smf.events.ui.actionandstatusdashboard

import android.app.Application
import androidx.lifecycle.liveData
import com.smf.events.base.BaseViewModel
import com.smf.events.helper.AppConstants
import com.smf.events.ui.dashboard.model.ActionAndStatusCount
import com.smf.events.ui.dashboard.model.MyEvents
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ActionsAndStatusViewModel @Inject constructor(
    private val actionsAndStatusRepository: ActionsAndStatusRepository,
    application: Application,
) : BaseViewModel(application) {

    var newRequestCount: Int = 0

    // 2560 Prepare Action List Values
    fun getActionsList(actionAndStatusData: ActionAndStatusCount?): ArrayList<MyEvents> {
        var list = ArrayList<MyEvents>()
        list.add(MyEvents(actionAndStatusData?.bidRequestedCount.toString(),
            AppConstants.NEW_REQUEST))
        newRequestCount = actionAndStatusData!!.bidRequestedCount
        list.add(
            MyEvents(
                actionAndStatusData.pendingForQuoteCount.toString(),
                AppConstants.PENDING_QUOTE
            )
        )
        list.add(MyEvents(actionAndStatusData.bidSubmittedCount.toString(),
            AppConstants.SUBMITTED_BID))
        list.add(MyEvents(actionAndStatusData.bidRejectedCount.toString(), AppConstants.REJECTED_BID))
        list.add(MyEvents(actionAndStatusData.wonBidCount.toString(), AppConstants.BID_WON))
        list.add(MyEvents(actionAndStatusData.lostBidCount.toString(), AppConstants.BID_LOST))
        return list
    }

    // 2560 Prepare Status List Values
    fun getStatusList(actionAndStatusData: ActionAndStatusCount): ArrayList<MyEvents> {
        var list = ArrayList<MyEvents>()
        list.add(
            MyEvents(
                "0",
                AppConstants.REQUEST_CLOSED
            )
        )
      //  list.add(MyEvents(actionAndStatusData.bidRejectedCount.toString(),
         //   AppConstants.REJECTED_BID))
        list.add(MyEvents(actionAndStatusData.bidTimedOutCount.toString(), AppConstants.TIMED_OUT))
       // list.add(MyEvents(actionAndStatusData.lostBidCount.toString(), AppConstants.BID_LOST))
        list.add(MyEvents("0", AppConstants.PENDING_FOR_REVIEW))
        return list
    }

    // Method For Getting Action And Status
    fun getActionAndStatus(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int?,
        serviceVendorOnboardingId: Int?,
    ) = liveData(Dispatchers.IO) {
        emit(
            actionsAndStatusRepository.getActionAndStatus(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnboardingId
            )
        )
    }
}