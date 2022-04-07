package com.smf.events.ui.bidrejectiondialog

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.actionandstatusdashboard.model.NewRequestList
import com.smf.events.ui.bidrejectiondialog.model.ServiceProviderBidRequestDto
import retrofit2.HttpException
import javax.inject.Inject

class BidRejectionRepository @Inject constructor(var apiStories: ApiStories) {

    suspend fun putBidRejection(
        idToken: String,
        serviceProviderBidRequestDto: ServiceProviderBidRequestDto,
    ): ApisResponse<NewRequestList> {

        return try {
            val getResponse = apiStories.putBidRejection(idToken, serviceProviderBidRequestDto)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }
}