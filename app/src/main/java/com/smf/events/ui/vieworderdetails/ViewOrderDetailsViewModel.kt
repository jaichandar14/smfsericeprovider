package com.smf.events.ui.vieworderdetails

import android.app.Application
import androidx.lifecycle.liveData
import com.smf.events.base.BaseDialogViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
//2402
class ViewOrderDetailsViewModel @Inject constructor(
    private val viewOrderDetailsRepository: ViewOrderDetailsRepository,
    application: Application,
) : BaseDialogViewModel(application) {
    //2402 - View Order Details Api Call
    fun getViewOrderDetails(idToken: String, eventId: Int, eventServiceDescriptionId: Int) =
        liveData(
            Dispatchers.IO) {
            emit(viewOrderDetailsRepository.getViewOrderDetails(idToken,
                eventId,
                eventServiceDescriptionId))
        }
}