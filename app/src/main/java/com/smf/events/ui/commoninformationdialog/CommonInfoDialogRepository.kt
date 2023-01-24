package com.smf.events.ui.commoninformationdialog

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.commoninformationdialog.model.StartService
import javax.inject.Inject

class CommonInfoDialogRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {
    // 2904  Status flow in QuoteDetails Api Call
    suspend fun updateServiceStatus(
        idToken: String,
        bidRequestId: Int,
        eventId: Int,
        eventServiceDescriptionId: Int,
        status: String,
    ): ApisResponse<StartService> {

        return safeApiCall {
            apiStories.updateServiceStatus(
                idToken,
                bidRequestId,
                eventId,
                eventServiceDescriptionId,
                status
            )
        }
    }
}