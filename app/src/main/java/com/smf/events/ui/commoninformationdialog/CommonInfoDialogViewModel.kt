package com.smf.events.ui.commoninformationdialog

import android.app.Application
import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smf.events.base.BaseDialogViewModel
import com.smf.events.helper.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class CommonInfoDialogViewModel @Inject constructor(
    private val commonInfoDialogRepository: CommonInfoDialogRepository,
    application: Application,
) : BaseDialogViewModel(application) {

    var TAG = "CommonInfoDialogViewModel"

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
            try {
                emit(
                    commonInfoDialogRepository.updateServiceStatus(
                        idToken,
                        bidRequestId,
                        eventId,
                        eventServiceDescriptionId,
                        status
                    )
                )
            } catch (e: Exception) {
                Log.d(TAG, "getActionAndStatus: UnknownHostException $e")
                when (e) {
                    is UnknownHostException -> {
                        Log.d(TAG, "getActionAndStatus: UnknownHostException $e")
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    is ConnectException -> {
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    else -> {}
                }
            }
        }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBack Interface
    interface CallBackInterface {
        fun internetError(exception: String)
    }
}