package com.smf.events.ui.intiateservicedialog

import android.app.Application
import androidx.lifecycle.liveData
import com.smf.events.base.BaseDialogViewModel
import com.smf.events.ui.commoninformationdialog.CommonInfoDialogRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class InitiateServiceDialogViewModel @Inject constructor(
    private val initiateServiceDialogRepository: InitiateServiceDialogRepository,
    application: Application,
) : BaseDialogViewModel(application) {

    // 2904  Status flow in QuoteDetails Api Call
    fun updateServiceStatus(
        idToken: String,
        bidRequestId: Int,
        eventId: Int,
        eventServiceDescriptionId: Int,
        status: String,
    ) = liveData(Dispatchers.IO) {
        emit(
            initiateServiceDialogRepository.updateServiceStatus(
                idToken,
                bidRequestId,
                eventId,
                eventServiceDescriptionId,
                status
            )
        )
    }
}