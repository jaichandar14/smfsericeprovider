package com.smf.events.ui.emailverificationcode

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.core.Amplify
import com.smf.events.base.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class EmailVerificationCodeViewModel @Inject constructor(application: Application) :
    BaseViewModel(application) {

    fun confirmUserAttribute(code: String) {
        // ConfirmUser
        Amplify.Auth.confirmUserAttribute(
            AuthUserAttributeKey.email(), code,
            {
                Log.i("AuthDemo", "Confirmed user attribute with correct code.")
                viewModelScope.launch {
                    callBackInterface?.callBack("EmailVerifiedGoToDashBoard")
                }
            },
            {
                Log.e("AuthDemo", "Failed to confirm user attribute. Bad code?", it)
                viewModelScope.launch {
                    var errMsg = it.cause!!.message!!.split(".")[0]
                    toastMessage = errMsg
                    callBackInterface!!.awsErrorResponse()
                }


            }
        )
    }

    // Resend Email Verification Code
    fun resendEmailVerification() {
        Amplify.Auth.resendUserAttributeConfirmationCode(AuthUserAttributeKey.email(),
            {
                Log.i("AuthDemo", "Code was sent again: $it")
            },
            {
                Log.e("AuthDemo", "Failed to resend code", it)
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

    // CallBackInterface
    interface CallBackInterface {
        fun callBack(status: String)
        fun awsErrorResponse()
    }

}