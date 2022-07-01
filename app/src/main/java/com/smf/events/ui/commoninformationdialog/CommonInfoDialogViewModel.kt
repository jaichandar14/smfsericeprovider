package com.smf.events.ui.commoninformationdialog

import android.app.Application
import androidx.lifecycle.liveData
import com.smf.events.base.BaseDialogViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class CommonInfoDialogViewModel @Inject constructor(
    private val commonInfoDialogRepository: CommonInfoDialogRepository,
    application: Application,
) : BaseDialogViewModel(application) {
    // 2904  Status flow in QuoteDetails Api Call
    fun updateServiceStatus(
        idToken: String,
        bidRequestId: Int,
        eventId: Int,
        eventServiceDescriptionId: Int,
        status: String,
    ) =
        liveData(
            Dispatchers.IO
        ) {
            emit(commonInfoDialogRepository.updateServiceStatus(idToken,
                bidRequestId,
                eventId,
                eventServiceDescriptionId,
                status))
        }
}