package com.smf.events.ui.timeslot.deselectingdialog

import com.smf.events.helper.ApisResponse
import com.smf.events.network.ApiStories
import com.smf.events.ui.timeslot.deselectingdialog.model.ModifyDaySlotResponse
import com.smf.events.ui.timeslotmodifyexpanablelist.model.ModifyBookedServiceEvents
import retrofit2.HttpException
import javax.inject.Inject

class DeselectingDialogRepository @Inject constructor(var apiStories: ApiStories) {

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
        return try {
            val getResponse = apiStories.getModifyDaySlot(
                idToken,
                spRegId,
                fromDate,
                isAvailable,
                modifiedSlot,
                serviceVendorOnBoardingId,
                toDate
            )
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
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
        return try {
            val getResponse = apiStories.getModifyWeekSlot(
                idToken,
                spRegId,
                fromDate,
                isAvailable,
                modifiedSlot,
                serviceVendorOnBoardingId,
                toDate
            )
            ApisResponse.Success(getResponse)
        } catch (e: HttpException) {
            ApisResponse.Error(e)
        }
    }
}