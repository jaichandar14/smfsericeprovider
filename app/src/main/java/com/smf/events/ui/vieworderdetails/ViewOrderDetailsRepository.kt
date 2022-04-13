package com.smf.events.ui.vieworderdetails

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.vieworderdetails.model.OrderDetails
import retrofit2.HttpException
import javax.inject.Inject

class ViewOrderDetailsRepository @Inject constructor(var apiStories: ApiStories) {
    // 2402
    suspend fun getViewOrderDetails(
        idToken: String,
        eventId: Int,
        eventServiceDescId: Int,
    ): ApisResponse<OrderDetails> {
        return try {
            val getResponse = apiStories.getViewOrderDetails(idToken, eventId, eventServiceDescId)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }
}