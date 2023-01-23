package com.smf.events.ui.quotebriefdialog

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.quotebrief.model.QuoteBrief
import com.smf.events.ui.quotebriefdialog.model.ViewQuotes
import javax.inject.Inject

class QuoteBriefDialogRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    suspend fun getQuoteBrief(idToken: String, bidRequestId: Int): ApisResponse<QuoteBrief> {
        return safeApiCall { apiStories.getQuoteBrief(idToken, bidRequestId) }
    }

    suspend fun getViewQuote(idToken: String, bidRequestId: Int): ApisResponse<ViewQuotes> {
        return safeApiCall { apiStories.getViewQuotes(idToken, bidRequestId) }
    }
}