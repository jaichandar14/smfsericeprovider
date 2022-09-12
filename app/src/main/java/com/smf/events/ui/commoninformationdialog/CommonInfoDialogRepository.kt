package com.smf.events.ui.commoninformationdialog

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.commoninformationdialog.model.StartService
import retrofit2.HttpException
import javax.inject.Inject

class CommonInfoDialogRepository @Inject constructor(var apiStories: ApiStories) {
    // 2904  Status flow in QuoteDetails Api Call
    suspend fun updateServiceStatus(
        idToken: String,
        bidRequestId: Int,
        eventId: Int,
        eventServiceDescriptionId: Int,
        status: String,
    ): ApisResponse<StartService> {

        return try {
            val getResponse = apiStories.updateServiceStatus(
                idToken,
                bidRequestId,
                eventId,
                eventServiceDescriptionId,
                status
            )
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }
}