package com.smf.events.ui.bidrejectiondialog

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.actionandstatusdashboard.model.NewRequestList
import com.smf.events.ui.bidrejectiondialog.model.ServiceProviderBidRequestDto
import javax.inject.Inject

class BidRejectionRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    suspend fun putBidRejection(
        idToken: String,
        serviceProviderBidRequestDto: ServiceProviderBidRequestDto,
    ): ApisResponse<NewRequestList> {
        return safeApiCall { apiStories.putBidRejection(idToken, serviceProviderBidRequestDto) }
    }
}