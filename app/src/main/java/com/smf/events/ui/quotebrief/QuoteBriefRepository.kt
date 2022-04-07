package com.smf.events.ui.quotebrief

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.quotebrief.model.QuoteBrief
import retrofit2.HttpException
import javax.inject.Inject

class QuoteBriefRepository @Inject constructor(var apiStories: ApiStories)  {

    suspend fun getQuoteBrief(idToken: String, bidRequestId: Int): ApisResponse<QuoteBrief> {

        return try {
            val getResponse = apiStories.getQuoteBrief(idToken, bidRequestId)
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }
}