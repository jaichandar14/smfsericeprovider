package com.smf.events.ui.timeslot.deselectingdialog

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

class DeselectingDialogViewModel @Inject constructor(
    private val deselectingDialogRepository: DeselectingDialogRepository,
    application: Application,
) : BaseDialogViewModel(application) {

    var TAG = "DeselectingDialogViewModel"

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
            try {
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
            }catch (e: Exception){
                Log.d(TAG, "getModifyMonthSlot: $e")
                when (e) {
                    is UnknownHostException -> {
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    is ConnectException ->{
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    else -> {}
                }
            }
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
            try {
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
            }catch (e: Exception){
                Log.d(TAG, "getModifyMonthSlot: $e")
                when (e) {
                    is UnknownHostException -> {
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    is ConnectException ->{
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    else -> {}
                }
            }
        }

    // 2823 - modify-month-slot
    fun getModifyMonthSlot(
        idToken: String,
        spRegId: Int,
        fromDate: String,
        isAvailable: Boolean,
        modifiedSlot: String,
        serviceVendorOnBoardingId: Int,
        toDate: String
    ) =
        liveData(Dispatchers.IO) {
            try {
                emit(
                    deselectingDialogRepository.getModifyMonthSlot(
                        idToken,
                        spRegId,
                        fromDate,
                        isAvailable,
                        modifiedSlot,
                        serviceVendorOnBoardingId,
                        toDate
                    )
                )
            }catch (e: Exception){
                Log.d(TAG, "getModifyMonthSlot: $e")
                when (e) {
                    is UnknownHostException -> {
                        viewModelScope.launch {
                            callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                        }
                    }
                    is ConnectException ->{
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