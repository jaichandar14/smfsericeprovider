package com.smf.events.ui.signin

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.smf.events.R
import com.smf.events.base.BaseViewModel
import com.smf.events.helper.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class SignInViewModel @Inject constructor(
    private val signInRepository: SignInRepository,
    application: Application
) : BaseViewModel(application) {

    var TAG = "SignInViewModel"
    val mobileNumber = MutableLiveData<String>()
    val getMobileNumber: LiveData<String> = mobileNumber
    val emailId = MutableLiveData<String>()
    val getEmailId: LiveData<String> = emailId

    //SignIn Method
    fun signIn(userName: String, context: Context) {
        Amplify.Auth.signIn(userName, null, { result ->
            if (result.isSignInComplete) {
                Log.i(TAG, "Sign in succeeded $result")
                viewModelScope.launch {
                    callBackInterface?.callBack("signInCompletedGoToDashBoard")
                }
            } else {
                Log.i(TAG, "Sign in not complete $result")
                viewModelScope.launch {
                    callBackInterface?.callBack("SignInNotCompleted")
                }
            }
        }, {
            Log.e(TAG, "Failed to sign in ${it.cause!!.message!!.split(".")[0]}")
            viewModelScope.launch {
                var errMsg = it.cause!!.message!!.split(".")[0]
                if (errMsg == context.resources.getString(R.string.CreateAuthChallenge_failed_with_error)) {
                    resendSignUp(userName, context)
                } else if (errMsg.contains(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp)) ||
                    errMsg.contains(context.resources.getString(R.string.Unable_to_resolve_host))
                ) {
                    callBackInterface!!.awsErrorResponse(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp))
                } else {
                    toastMessage = errMsg
                    callBackInterface!!.awsErrorResponse(it.cause!!.message!!.split(".")[0])
                }
            }
        })
    }

    // ResendSignUpCode
    fun resendSignUp(userName: String, context: Context) {
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
                    val errMsg = it.cause!!.message!!.split(".")[0]
                    toastMessage = errMsg
                    if (errMsg.contains(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp)) ||
                        errMsg.contains(context.resources.getString(R.string.Unable_to_resolve_host))
                    ) {
                        callBackInterface!!.awsErrorResponse(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp))
                    }
                }
            })
    }

    // Getting UserDetails During SignIn
    fun getUserDetails(loginName: String) = liveData(Dispatchers.IO) {
        try {
            Log.d("TAG", "setUserDetails: $loginName")
            emit(signInRepository.getUserDetails(loginName))
        } catch (e: Exception) {
            Log.d("TAG", "getActionAndStatus: UnknownHostException $e")
            when (e) {
                is UnknownHostException -> {
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

    // CallBackInterface
    interface CallBackInterface {
        fun callBack(status: String)
        fun awsErrorResponse(message: String)
        fun internetError(exception: String)
    }
}
