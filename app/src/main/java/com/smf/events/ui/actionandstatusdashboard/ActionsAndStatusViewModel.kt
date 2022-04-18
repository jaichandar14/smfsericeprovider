package com.smf.events.ui.actionandstatusdashboard

import android.app.Application
import androidx.lifecycle.liveData
import com.smf.events.base.BaseViewModel
import com.smf.events.ui.dashboard.model.ActionAndStatusCount
import com.smf.events.ui.dashboard.model.MyEvents
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ActionsAndStatusViewModel @Inject constructor(
    private val actionsAndStatusRepository: ActionsAndStatusRepository,
    application: Application,
) : BaseViewModel(application) {

    var newRequestCount: Int = 0

    // Prepare Action List Values
    fun getActionsList(actionAndStatusData: ActionAndStatusCount?): ArrayList<MyEvents> {
        var list = ArrayList<MyEvents>()
        list.add(MyEvents(actionAndStatusData?.bidRequestedActionsCount.toString(), "New request"))
        newRequestCount = actionAndStatusData!!.bidRequestedActionsCount
        list.add(
            MyEvents(
                actionAndStatusData.pendingForQuoteActionCount.toString(),
                "Pending Quote"
            )
        )
        list.add(MyEvents(actionAndStatusData.wonBidStatusCount.toString(), "Won Bid"))
        list.add(MyEvents(actionAndStatusData.bidRejectedActionCount.toString(), "Rejected"))
        list.add(MyEvents(actionAndStatusData.bidSubmittedActionCount.toString(), "Bid Submitted"))
        list.add(MyEvents(actionAndStatusData.pendingForReview.toString(), "Pending    For Review"))

        return list

    }

    // Prepare Status List Values
    fun getStatusList(actionAndStatusData: ActionAndStatusCount): ArrayList<MyEvents> {
        var list = ArrayList<MyEvents>()
        list.add(
            MyEvents(
                actionAndStatusData.bidSubmittedStatusCount.toString(),
                "Bids Submitted"
            )
        )
        list.add(MyEvents(actionAndStatusData.serviceDoneStatusCount.toString(), "Service done"))
        list.add(MyEvents(actionAndStatusData.bidTimedOutStatusCount.toString(), "Bid TimeOut"))
        list.add(MyEvents(actionAndStatusData.wonBidStatusCount.toString(), "Won Bid"))
        list.add(MyEvents(actionAndStatusData.bidRejectedStatusCount.toString(), "Rejected"))
        list.add(MyEvents(actionAndStatusData.lostBidStatusCount.toString(), "Lost Bid"))
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