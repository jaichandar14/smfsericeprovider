package com.smf.events.ui.quotedetailsdialog

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.actionandstatusdashboard.model.NewRequestList
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
import javax.inject.Inject

class QuoteDetailsRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    suspend fun postQuoteDetails(
        idToken: String,
        bidRequestId: Int,
        biddingQuote: BiddingQuotDto,
    ): ApisResponse<NewRequestList> {
        return safeApiCall { apiStories.postQuoteDetails(idToken, bidRequestId, biddingQuote) }
    }
}