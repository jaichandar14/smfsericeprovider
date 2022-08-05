package com.smf.events.ui.quotedetailsdialog

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.actionandstatusdashboard.model.NewRequestList
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
import retrofit2.HttpException
import javax.inject.Inject

class QuoteDetailsRepository @Inject constructor(var apiStories: ApiStories) {

    suspend fun postQuoteDetails(
        idToken: String,
        bidRequestId: Int,
        biddingQuote: BiddingQuotDto,
    ): ApisResponse<NewRequestList> {
        return try {
            val getResponse = apiStories.postQuoteDetails(idToken, bidRequestId, biddingQuote)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }
}