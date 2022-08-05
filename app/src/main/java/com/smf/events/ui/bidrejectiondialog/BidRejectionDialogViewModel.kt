package com.smf.events.ui.bidrejectiondialog

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smf.events.base.BaseDialogViewModel
import com.smf.events.databinding.FragmentBidRejectionDialogBinding
import com.smf.events.helper.AppConstants
import com.smf.events.ui.bidrejectiondialog.model.ServiceProviderBidRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class BidRejectionDialogViewModel @Inject constructor(
    private val bidRejectionRepository: BidRejectionRepository,
    application: Application,
) : BaseDialogViewModel(application) {

    @SuppressLint("ResourceType")
    fun reasonForReject(mDataBinding: FragmentBidRejectionDialogBinding?) {
        // 2405 - As Per Ticket Condition resources ArrayList Values Changed
        var resources: ArrayList<String> = ArrayList()
        resources.add(0, "Already booked for this day")
        resources.add(1, "Budget too low")
        resources.add(2, "Venue too far to provide service")
        resources.add(3, "Other")
        val spin = mDataBinding!!.spnReason
        spin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                callBackInterface?.callBack(resources[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        // 2974
        val ad: ArrayAdapter<String> =
            ArrayAdapter<String>(getApplication(), com.smf.events.R.layout.spinners_list, resources)
        // set simple layout resource file
        // for each item of spinner
        ad.setDropDownViewResource(
            com.smf.events.R.layout.spinners_list
        )

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        spin.adapter = ad
    }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBack Interface
    interface CallBackInterface {
        fun callBack(status: String)
        fun internetError(exception: String)
    }

    fun putBidRejection(
        idToken: String,
        serviceProviderBidRequestDto: ServiceProviderBidRequestDto,
    ) = liveData(
        Dispatchers.IO
    ) {
        try {
            emit(bidRejectionRepository.putBidRejection(idToken, serviceProviderBidRequestDto))
        } catch (e: Exception) {
            Log.d("TAG", "putBidRejection: $e")
            when (e) {
                is UnknownHostException -> {
                    Log.d("TAG", "getBookedEventServices when mody called: $e")
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
                is ConnectException -> {
                    viewModelScope.launch {
                        callBackInterface?.internetError(AppConstants.UNKOWNHOSTANDCONNECTEXCEPTION)
                    }
                }
            }
        }
    }
}