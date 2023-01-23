package com.smf.events.ui.quotebrief

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.quotebrief.model.QuoteBrief
import retrofit2.HttpException
import javax.inject.Inject

class QuoteBriefRepository @Inject constructor(var apiStories: ApiStories): BaseRepo() {

    suspend fun getQuoteBrief(idToken: String, bidRequestId: Int): ApisResponse<QuoteBrief> {
        return safeApiCall { apiStories.getQuoteBrief(idToken, bidRequestId) }
    }
}