package com.smf.events.ui.emailotp

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseViewModel
import com.smf.events.databinding.FragmentEmailOtpBinding
import com.smf.events.helper.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class EmailOTPViewModel @Inject constructor(
    private val eMailOTPRepository: EmailOTPRepository,
    application: Application,
) : BaseViewModel(application) {

    private val TAG = "EmailOTPViewModel"
    val userOtpNumber = MutableLiveData<String>()
    private var idToken: String? = null

    // Custom Confirm SignIn Function
    fun confirmSignIn(otp: String, mDataBinding: FragmentEmailOtpBinding) {
        Amplify.Auth.confirmSignIn(otp,
            {
                // Aws method for Fetching Id Token
                fetchIdToken()
                //Aws Method for 6 digit Validation Check
                emailCodeValidationCheck()
            },
            {
                Log.e(
                    TAG,
                    "Failed to confirm signIn ${it.cause!!.message!!.split(".")[0]}",
                    it
                )
                viewModelScope.launch {
                    val errMsg = mDataBinding.otpemail.text.toString()
                    if (errMsg.isEmpty()) {
                        toastMessage = AppConstants.ENTER_OTP
                        callBackInterface!!.awsErrorResponse()
                    } else {
                        toastMessage = AppConstants.INVALID_OTP
                        callBackInterface!!.awsErrorResponse()
//                        callBackInterface!!.navigatingPage()
                    }
                }
            })
    }

    // Aws method for Fetching Id Token
    private fun fetchIdToken() {
        Amplify.Auth.fetchAuthSession(
            {
                val session = it as AWSCognitoAuthSession
                idToken = AuthSessionResult.success(session.userPoolTokens.value?.idToken).value
                setTokenToSharedPref(idToken)
            },
            { Log.e(TAG, "Failed to fetch session", it) }
        )
    }

    // Method for save IdToken
    private fun setTokenToSharedPref(token: String?) {
        val sharedPreferences =
            getApplication<SMFApp>().getSharedPreferences(AppConstants.MY_USER, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(AppConstants.ID_TOKEN, token)
        editor?.apply()
    }

    // Aws Method for 6 digit Validation Check
    private fun emailCodeValidationCheck() {
        Amplify.Auth.fetchUserAttributes(
            { result ->
                if (result[1].value.equals(AppConstants.FALSE)) {
                    eMailVerification()
                } else {
                    Log.i(TAG, "User attributes = successfully entered dashboard")
                    viewModelScope.launch {
                        callBackInterface?.callBack("EMailVerifiedTrueGoToDashBoard")
                    }
                }
            },
            {
                Log.e(TAG, "Failed to fetch user attributes", it)
                viewModelScope.launch {
                    toastMessage = AppConstants.INVALID_OTP
                    callBackInterface!!.awsErrorResponse()
                }
            })
    }

    // Method For Getting Service Provider Reg Id and Role Id
    fun getLoginInfo(idToken: String) = liveData(Dispatchers.IO) {
        emit(eMailOTPRepository.getLoginInfo(idToken))
    }

    // Email Verification
    private fun eMailVerification() {
        Amplify.Auth.resendUserAttributeConfirmationCode(AuthUserAttributeKey.email(),
            {
                viewModelScope.launch {
                    callBackInterface?.callBack("goToEmailVerificationCodePage")
                }
            },
            {
                Log.e(TAG, "Failed to resend code", it)
                viewModelScope.launch {
                    val errMsg = it.cause!!.message!!.split(".")[0]
                    toastMessage = errMsg
                    callBackInterface!!.awsErrorResponse()
                }
            })
    }

    // 2351 Android-OTP expires Validation
    // OTP Resend SignIn Method
    fun reSendOTP(userName: String, mDataBinding: FragmentEmailOtpBinding) {
        Amplify.Auth.signIn(userName, null, {
            Log.d(TAG, "reSendOTP: called code resented successfully")
            viewModelScope.launch {
                otpTimerValidation(mDataBinding, userName)
            }
        },
            {
                Log.e(TAG, "Failed to sign in", it)
                viewModelScope.launch {
                    val errMsg = it.cause!!.message!!.split(".")[0]
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
        suspend fun callBack(status: String)
        fun awsErrorResponse()
        fun navigatingPage()
    }

    // 2351 Android-OTP expires Validation Method
    fun otpTimerValidation(mDataBinding: FragmentEmailOtpBinding?, userName: String) {
        var counter = 30
        val countTime: TextView = mDataBinding!!.otpTimer
        mDataBinding.otpResend.setTextColor(
            ContextCompat.getColor(
                getApplication(), R.color.buttoncolor
            )
        )
        mDataBinding.otpResend.isClickable = false
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (counter < 10) {
                    countTime.text = " 00:0${counter}"
                } else {
                    countTime.text = " 00:${counter}"
                }
                counter--
            }

            override fun onFinish() {
                mDataBinding.otpResend.setTextColor(
                    ContextCompat.getColor(
                        getApplication(), R.color.button_blue
                    )
                )
                mDataBinding.otpResend.isClickable = true
                countTime.text = AppConstants.INITIAL_TIME
                mDataBinding.otpResend.setOnClickListener {
                    reSendOTP(userName, mDataBinding)
                }
            }
        }.start()
    }
}