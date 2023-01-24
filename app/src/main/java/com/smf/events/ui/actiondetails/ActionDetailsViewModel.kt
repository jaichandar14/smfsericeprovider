package com.smf.events.ui.actiondetails

import android.app.Application
import androidx.lifecycle.liveData
import com.smf.events.base.BaseViewModel
import com.smf.events.ui.actionandstatusdashboard.model.ServiceProviderBidRequestDto
import com.smf.events.ui.actiondetails.model.ActionDetails
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ActionDetailsViewModel @Inject constructor(
    private val actionDetailsRepository: ActionDetailsRepository,
    application: Application,
) : BaseViewModel(application) {

    // Method For Prepare ActionDetails List
    fun getActionsDetailsList(myList: ArrayList<ServiceProviderBidRequestDto>): ArrayList<ActionDetails> {
        val list = ArrayList<ActionDetails>()
        for (i in myList.indices) {
            list.add(
                ActionDetails(
                    myList[i].bidRequestId,
                    myList[i].serviceCategoryId,
                    myList[i].eventId,
                    myList[i].eventDate,
                    myList[i].eventName,
                    myList[i].serviceName,
                    myList[i].serviceDate,
                    myList[i].bidRequestedDate,
                    myList[i].biddingCutOffDate,
                    // 2354
                    myList[i].currencyType,
                    myList[i].costingType,
                    myList[i].cost,
                    myList[i].latestBidValue,
                    myList[i].bidStatus,
                    myList[i].isExistingUser,
                    myList[i].eventServiceDescriptionId,
                    myList[i].branchName,
                    myList[i].timeLeft
                )
            )
        }
        return list
    }

    //Method For put QuoteDetails
    fun postQuoteDetails(idToken: String, bidRequestId: Int, biddingQuote: BiddingQuotDto) =
        liveData(
            Dispatchers.IO
        ) {
            emit(actionDetailsRepository.postQuoteDetails(idToken, bidRequestId, biddingQuote))
        }

    // Method For Get New Request
    fun getBidActions(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int?,
        serviceVendorOnboardingId: Int?,
        bidStatus: String,
    ) = liveData(Dispatchers.IO) {
            emit(
                actionDetailsRepository.getBidActions(
                    idToken,
                    spRegId,
                    serviceCategoryId,
                    serviceVendorOnboardingId,
                    bidStatus
                )
            )
        }

}