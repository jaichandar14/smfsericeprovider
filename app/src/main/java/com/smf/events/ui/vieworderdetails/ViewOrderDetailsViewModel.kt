package com.smf.events.ui.vieworderdetails

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

class ViewOrderDetailsViewModel @Inject constructor(
    private val viewOrderDetailsRepository: ViewOrderDetailsRepository,
    application: Application,
) : BaseDialogViewModel(application) {

    var TAG = "ViewOrderDetailsViewModel"

    // 2402 - View Order Details Api Call
    fun getViewOrderDetails(idToken: String, eventId: Int, eventServiceDescriptionId: Int) =
        liveData(
            Dispatchers.IO
        ) {
            try {
                emit(
                    viewOrderDetailsRepository.getViewOrderDetails(
                        idToken,
                        eventId,
                        eventServiceDescriptionId
                    )
                )
            } catch (e: Exception) {
                Log.d(TAG, "getViewOrderDetails: $e")
                when (e) {
                    is UnknownHostException -> {
                        Log.d("TAG", "getActionAndStatus: UnknownHostException $e")
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