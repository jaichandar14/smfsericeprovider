package com.smf.events.ui.actionandstatusdashboard

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.dashboard.model.ActionAndStatus
import javax.inject.Inject

class ActionsAndStatusRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    // Method For Get Action And Status
    suspend fun getActionAndStatus(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int?,
        serviceVendorOnboardingId: Int?,
    ): ApisResponse<ActionAndStatus> {
        return safeApiCall {
            apiStories.getActionAndStatus(
                idToken,
                spRegId,
                serviceCategoryId,
                serviceVendorOnboardingId
            )
        }
    }

}