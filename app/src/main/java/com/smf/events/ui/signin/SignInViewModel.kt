package com.smf.events.ui.signin

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.smf.events.base.BaseViewModel
import com.smf.events.ui.schedulemanagement.ScheduleManagementViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInViewModel @Inject constructor(
    private val signInRepository: SignInRepository,
    application: Application
) : BaseViewModel(application) {

    val mobileNumber = MutableLiveData<String>()
    val getMobileNumber : LiveData<String> = mobileNumber
    val emailId = MutableLiveData<String>()
    val getEmailId : LiveData<String> = emailId

    //SignIn Method
    fun signIn(userName: String) {
        Amplify.Auth.signIn(userName, null, { result ->
            if (result.isSignInComplete) {
                Log.i("AuthQuickstart", "Sign in succeeded $result")
                viewModelScope.launch {
                    callBackInterface?.callBack("signInCompletedGoToDashBoard")
                }
            } else {
                Log.i("AuthQuickstart", "Sign in not complete $result")
                viewModelScope.launch {
                    callBackInterface?.callBack("SignInNotCompleted")
                }
            }
        }, {
            Log.e("AuthQuickstart", "Failed to sign in${it.cause!!.message!!.split(".")[0]}")
            viewModelScope.launch {
                var errMsg = it.cause!!.message!!.split(".")[0]
                if (errMsg == "CreateAuthChallenge failed with error PhoneNumber not Verified") {
                    resendSignUp(userName)
                } else {
                    toastMessage = errMsg
                    callBackInterface!!.awsErrorResponse()
                }
            }
        })
    }



    // ResendSignUpCode
    private fun resendSignUp(userName: String) {
        Amplify.Auth.resendSignUpCode(userName,
            { result ->
                var status: String? = null
                status = if (result.isSignUpComplete) {
                    Log.d("TAG", "resendSignUp: success called")
                    "resend success"
                } else {
                    Log.d("TAG", "resendSignUp: else block called")
                    "resend failure"
                }
                callBackInterface!!.callBack(status)

            }, {
                Log.e("TAG", "resendSignUp: error called${it.cause!!.message!!.split(".")[0]}", it)
                viewModelScope.launch {
                    var errMsg = it.cause!!.message!!.split(".")[0]
                    toastMessage = errMsg
                    callBackInterface!!.awsErrorResponse()
                }
            })
    }

    // Getting UserDetails During SignIn
    fun getUserDetails(loginName: String) = liveData(Dispatchers.IO) {
        Log.d("TAG", "setUserDetails: $loginName")
        emit(signInRepository.getUserDetails(loginName))
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
