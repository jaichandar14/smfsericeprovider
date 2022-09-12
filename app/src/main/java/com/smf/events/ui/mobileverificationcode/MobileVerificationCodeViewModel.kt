package com.smf.events.ui.mobileverificationcode

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.smf.events.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MobileVerificationCodeViewModel @Inject constructor(application: Application) :
    BaseViewModel(application) {

    fun confirmSignUpFunctionality(userName: String, code: String) {

        //confirmSignUp
        Amplify.Auth.confirmSignUp(
            userName, code,
            { result ->

                Log.i("AuthQuickstart", "Confirm signUp succeeded $result")
                var status: String? = null
                status = if (result.isSignUpComplete) {
                    Log.i("AuthQuickstart", "Confirm signUp succeeded")
                    "Confirm signUp succeeded"
                } else {
                    Log.i("AuthQuickstart", "Confirm sign up not complete")
                    "Confirm sign up not complete"

                }

                viewModelScope.launch {
                    callBackInterface?.callBack(status, "SignUpCompleted")
                }
            },
            {
                Log.e("AuthQuickstart", "Failed to confirm sign up", it)
                viewModelScope.launch {
                    callBackInterface?.callBack("Failed to confirm sign up", "SignUpFailed")

                    var errMsg = it.cause!!.message!!.split(".")[0]
                    toastMessage = errMsg
                    callBackInterface!!.awsErrorResponse()

                }
            }
        )
    }

    fun resendSignUp(userName: String) {
        // ResendSignUpCode

        Amplify.Auth.resendSignUpCode(userName,
            { result ->
                if (result.isSignUpComplete) {
                    Log.d("TAG", "resendSignUp: success called")

                } else {
                    Log.d("TAG", "resendSignUp: else block called")
                }

            }, {
                Log.e("TAG", "resendSignUp: error called", it)
                viewModelScope.launch {
                    var errMsg = it.cause!!.message!!.split(".")[0]
                    toastMessage = errMsg
                    callBackInterface!!.awsErrorResponse()
                }
            })
    }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBack Interface
    interface CallBackInterface {
        fun callBack(status: String, flag: String)
        fun awsErrorResponse()

    }

}