package com.smf.events.ui.actionandstatusdashboard

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.dashboard.model.ActionAndStatus
import retrofit2.HttpException
import javax.inject.Inject

class ActionsAndStatusRepository @Inject constructor(var apiStories: ApiStories) {

    // Method For Get Action And Status
    suspend fun getActionAndStatus(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int?,
        serviceVendorOnboardingId: Int?,
    ): ApisResponse<ActionAndStatus> {

        return try {
            val getResponse = apiStories.getActionAndStatus(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnboardingId
            )
            ApisResponse.Success(getResponse)

        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }


}