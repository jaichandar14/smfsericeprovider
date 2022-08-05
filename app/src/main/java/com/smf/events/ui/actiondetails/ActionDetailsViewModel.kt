package com.smf.events.ui.actiondetails

import android.app.Application
import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smf.events.base.BaseViewModel
import com.smf.events.helper.AppConstants
import com.smf.events.ui.actionandstatusdashboard.model.ServiceProviderBidRequestDto
import com.smf.events.ui.actiondetails.model.ActionDetails
import com.smf.events.ui.quotedetailsdialog.model.BiddingQuotDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class ActionDetailsViewModel @Inject constructor(
    private val actionDetailsRepository: ActionDetailsRepository,
    application: Application,
) : BaseViewModel(application) {

    // Method For Prepare ActionDetails List
    fun getActionsDetailsList(myList: ArrayList<ServiceProviderBidRequestDto>): ArrayList<ActionDetails> {
        var list = ArrayList<ActionDetails>()
        for (i in myList.indices) {
            list.add(
                ActionDetails(
                    myList[i].bidRequestId,
                    myList[i].serviceCategoryId,
                    myList[i].eventId,
                    myList[i].eventDate,
                    myList[i].eventName,
                    myList[i].serviceName,
                    myList[i].serviceDate,
                    myList[i].bidRequestedDate,
                    myList[i].biddingCutOffDate,
                    // 2354
                    myList[i].currencyType,
                    myList[i].costingType,
                    myList[i].cost,
                    myList[i].latestBidValue,
                    myList[i].bidStatus,
                    myList[i].isExistingUser,
                    myList[i].eventServiceDescriptionId,
                    myList[i].branchName,
                    myList[i].timeLeft
                )
            )
        }
        return list
    }

    //Method For put QuoteDetails
    fun postQuoteDetails(idToken: String, bidRequestId: Int, biddingQuote: BiddingQuotDto) =
        liveData(
            Dispatchers.IO
        ) {
            try {
                emit(actionDetailsRepository.postQuoteDetails(idToken, bidRequestId, biddingQuote))
            }catch (e: Exception){
                Log.d("TAG", "postQuoteDetails: $e")
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
                }
            }
        }

    // Method For Get New Request
    fun getBidActions(
        idToken: String,
        spRegId: Int,
        serviceCategoryId: Int?,
        serviceVendorOnboardingId: Int?,
        bidStatus: String,
    ) =
        liveData(Dispatchers.IO) {
            try {
                emit(
                    actionDetailsRepository.getBidActions(
                        idToken,
                        spRegId,
                        serviceCategoryId,
                        serviceVendorOnboardingId,
                        bidStatus
                    )
                )
            }catch (e: Exception){
                Log.d("TAG", "getBidActions: $e")
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
                }
            }
        }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setViewModelCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBack Interface
    interface CallBackInterface {
        fun internetError(exception: String)
    }
}