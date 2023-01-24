package com.smf.events.ui.timeslot.deselectingdialog

import com.smf.events.base.BaseRepo
import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.timeslot.deselectingdialog.model.ModifyDaySlotResponse
import javax.inject.Inject

class DeselectingDialogRepository @Inject constructor(var apiStories: ApiStories) : BaseRepo() {

    // 2814 - modify-day-slot
    suspend fun getModifyDaySlot(
        idToken: String,
        spRegId: Int,
        fromDate: String,
        isAvailable: Boolean,
        modifiedSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String
    ): ApisResponse<ModifyDaySlotResponse> {
        return safeApiCall {
            apiStories.getModifyDaySlot(
                idToken,
                spRegId,
                fromDate,
                isAvailable,
                modifiedSlot,
                serviceVendorOnBoardingId,
                toDate
            )
        }
    }

    // 2815 - modify-week-slot
    suspend fun getModifyWeekSlot(
        idToken: String,
        spRegId: Int,
        fromDate: String,
        isAvailable: Boolean,
        modifiedSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String
    ): ApisResponse<ModifyDaySlotResponse> {
        return safeApiCall {
            apiStories.getModifyWeekSlot(
                idToken,
                spRegId,
                fromDate,
                isAvailable,
                modifiedSlot,
                serviceVendorOnBoardingId,
                toDate
            )
        }
    }

    // 2823 - modify-month-slot
    suspend fun getModifyMonthSlot(
        idToken: String,
        spRegId: Int,
        fromDate: String,
        isAvailable: Boolean,
        modifiedSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String
    ): ApisResponse<ModifyDaySlotResponse> {
        return safeApiCall {
            apiStories.getModifyMonthSlot(
                idToken,
                spRegId,
                fromDate,
                isAvailable,
                modifiedSlot,
                serviceVendorOnBoardingId,
                toDate
            )
        }
    }
}