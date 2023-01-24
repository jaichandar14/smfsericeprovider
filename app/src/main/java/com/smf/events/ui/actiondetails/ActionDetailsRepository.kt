package com.smf.events.ui.actiondetails

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.actionandstatusdashboard.model.NewRequestList
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
import javax.inject.Inject

class ActionDetailsRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    suspend fun postQuoteDetails(
        idToken: String,
        bidRequestId: Int,
        biddingQuote: BiddingQuotDto,
    ): ApisResponse<NewRequestList> {
        return safeApiCall { apiStories.postQuoteDetails(idToken, bidRequestId, biddingQuote) }
    }

    // Method For Get New Request
    suspend fun getBidActions(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int?,
        serviceVendorOnboardingId: Int?,
        bidStatus: String,
    ): ApisResponse<NewRequestList> {
        val bidStatusList = ArrayList<String>()
        bidStatusList.add(bidStatus)
        return safeApiCall {
            apiStories.getBidActions(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnboardingId,
                bidStatusList
            )
        }
    }
}