package com.smf.events.ui.vieworderdetails

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.vieworderdetails.model.OrderDetails
import javax.inject.Inject

class ViewOrderDetailsRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {
    // 2402
    suspend fun getViewOrderDetails(
        idToken: String,
        eventId: Int,
        eventServiceDescId: Int,
    ): ApisResponse<OrderDetails> {
        return safeApiCall { apiStories.getViewOrderDetails(idToken, eventId, eventServiceDescId) }
    }
}