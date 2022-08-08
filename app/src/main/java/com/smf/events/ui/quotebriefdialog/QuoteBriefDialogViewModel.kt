package com.smf.events.ui.quotebriefdialog

import android.app.Application
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.smf.events.base.BaseDialogViewModel
import com.smf.events.databinding.FragmentQuoteBriefBinding
import com.smf.events.databinding.QuoteBriefDialogBinding
import com.smf.events.helper.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class QuoteBriefDialogViewModel @Inject constructor(
    private val quoteBriefDialogRepository: QuoteBriefDialogRepository,
    application: Application,
) : BaseDialogViewModel(application) {

    var TAG = "QuoteBriefDialogViewModel"

    fun expandableView(mDataBinding: QuoteBriefDialogBinding?, expand: Boolean) {
        var exp = false
        val isExpandable: Boolean = expand

        mDataBinding!!.expBtn.setOnClickListener {
            if (isExpandable == exp) {
                Log.d("TAG", "expandableView: true ")
                mDataBinding.expandableView.visibility = View.VISIBLE
            } else {
                mDataBinding.expandableView.visibility = View.GONE
            }
            exp = !exp
        }
    }

    fun progress2Completed(mDataBinding: FragmentQuoteBriefBinding?) {
        mDataBinding!!.check2Complete.visibility = View.VISIBLE
        mDataBinding.check3Inprogress.visibility = View.VISIBLE
        mDataBinding.processflow2.setBackgroundColor(Color.BLACK)
    }

    fun progress3Completed(mDataBinding: FragmentQuoteBriefBinding?) {
        mDataBinding!!.check3Completed.visibility = View.VISIBLE
        mDataBinding.check4Inprogress.visibility = View.VISIBLE
        mDataBinding.processflow3.setBackgroundColor(Color.BLACK)
    }

    fun progress4Completed(mDataBinding: FragmentQuoteBriefBinding?) {
        mDataBinding!!.check4Completed.visibility = View.VISIBLE
    }

    fun getQuoteBrief(idToken: String, bidRequestId: Int) = liveData(
        Dispatchers.IO) {
        try {
            emit(quoteBriefDialogRepository.getQuoteBrief(idToken, bidRequestId))
        }catch (e: Exception){
            Log.d(TAG, "getQuoteBrief: $e")
            when (e) {
                is UnknownHostException -> {
                    Log.d("TAG", "getActionAndStatus: UnknownHostException $e")
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

    fun getViewQuote(idToken: String, bidRequestId: Int) = liveData(
        Dispatchers.IO) {
        try {
            emit(quoteBriefDialogRepository.getViewQuote(idToken, bidRequestId))
        }catch (e: Exception){
            Log.d(TAG, "getViewQuote: $e")
            when (e) {
                is UnknownHostException -> {
                    Log.d("TAG", "getActionAndStatus: UnknownHostException $e")
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