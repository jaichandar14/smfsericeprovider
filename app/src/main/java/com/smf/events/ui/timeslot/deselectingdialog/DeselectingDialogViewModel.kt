package com.smf.events.ui.timeslot.deselectingdialog

import android.app.Application
import androidx.lifecycle.liveData
import com.smf.events.base.BaseDialogViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class DeselectingDialogViewModel @Inject constructor(
    private val deselectingDialogRepository: DeselectingDialogRepository,
    application: Application,
) : BaseDialogViewModel(application) {

    // 2814 - modify-day-slot
    fun getModifyDaySlot(
        idToken: String,
        spRegId: Int,
        fromDate: String,
        isAvailable: Boolean,
        modifiedSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String
    ) =
        liveData(Dispatchers.IO) {
            emit(
                deselectingDialogRepository.getModifyDaySlot(
                    idToken,
                    spRegId,
                    fromDate,
                    isAvailable,
                    modifiedSlot,
                    serviceVendorOnBoardingId,
                    toDate
                )
            )
        }

    // 2814 - modify-week-slot
    fun getModifyWeekSlot(
        idToken: String,
        spRegId: Int,
        fromDate: String,
        isAvailable: Boolean,
        modifiedSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String
    ) =
        liveData(Dispatchers.IO) {
            emit(
                deselectingDialogRepository.getModifyWeekSlot(
                    idToken,
                    spRegId,
                    fromDate,
                    isAvailable,
                    modifiedSlot,
                    serviceVendorOnBoardingId,
                    toDate
                )
            )
        }
}