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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class EmailOTPViewModel @Inject constructor(
    private val eMailOTPRepository: EmailOTPRepository,
    application: Application,
) : BaseViewModel(application) {

    private val TAG = "EmailOTPViewModel"
    val userOtpNumber = MutableLiveData<String>()
    val userOtp1 = MutableLiveData<String>()
    val userOtp2 = MutableLiveData<String>()
    val userOtp3 = MutableLiveData<String>()
    val userOtp4 = MutableLiveData<String>()

    var resendRestriction = 0

    private var idToken: String? = null
    var num = 0

    // Custom Confirm SignIn Function
    fun confirmSignIn(context: Context, otp: String, mDataBinding: FragmentEmailOtpBinding) {
        num += 1
        Amplify.Auth.confirmSignIn(otp,
            {
                Log.d(TAG, "confirmSignIn scess: $it")
                if (it.isSignInComplete) {
                    // Aws method for Fetching Id Token
                    fetchIdToken(context)
                } else {
                    Log.d(TAG, "confirmSignIn scess else: $it")
                    viewModelScope.launch {
                        toastMessage = AppConstants.INVALID_OTP
                        callBackInterface?.awsErrorResponse(num.toString())
                    }
                }
            },
            {
                Log.d(TAG, "Failed to confirm signIn ${it.localizedMessage}", it)
                val errMsg = it.cause!!.message!!.split(".")[0]
                viewModelScope.launch {
                    if (it.cause?.message?.contains(context.resources.getString(R.string.OTP_expired)) == true || it.cause?.message?.contains(
                            context.resources.getString(R.string.Invalid_session_for_the_user)
                        ) == true
                    ) {
                        toastMessage = context.resources.getString(R.string.OTP_is_expired)
                        callBackInterface!!.awsErrorResponse(num.toString())
                    } else if (errMsg.contains(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp)) ||
                        errMsg.contains(context.resources.getString(R.string.Unable_to_resolve_host))
                    ) {
                        callBackInterface!!.awsErrorResponse(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp))
                    } else {
                        toastMessage = AppConstants.INVALID_OTP
                        callBackInterface!!.awsErrorResponse(num.toString())
//                        callBackInterface?.otpValidation(false)
                    }
                }
            })
    }

    // Aws method for Fetching Id Token
    private fun fetchIdToken(context: Context) {
        Amplify.Auth.fetchAuthSession(
            {
                val session = it as AWSCognitoAuthSession
                idToken = AuthSessionResult.success(session.userPoolTokens.value?.idToken).value
                setTokenToSharedPref(idToken)
                Log.d(TAG, "confirmSignIn scess idToken: $it, $idToken")
                viewModelScope.launch {
                    //Aws Method for 6 digit Validation Check
                    emailCodeValidationCheck(context)
                }
            },
            {
                Log.e(TAG, "Failed to fetch session", it)
                val errMsg = it.cause!!.message!!.split(".")[0]
                viewModelScope.launch {
                    if (errMsg.contains(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp)) ||
                        errMsg.contains(context.resources.getString(R.string.Unable_to_resolve_host))
                    ) {
                        callBackInterface!!.awsErrorResponse(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp))
                    }
                }
            }
        )
    }

    // Method for save IdToken
    private fun setTokenToSharedPref(token: String?) {
        val sharedPreferences =
            getApplication<SMFApp>().getSharedPreferences(
                AppConstants.MY_USER,
                Context.MODE_PRIVATE
            )
        val editor = sharedPreferences?.edit()
        editor?.putString(AppConstants.ID_TOKEN, token)
        editor?.apply()
    }

    // Aws Method for 6 digit Validation Check
    private fun emailCodeValidationCheck(context: Context) {
        Amplify.Auth.fetchUserAttributes(
            { result ->
                if (result[1].value.equals(AppConstants.FALSE)) {
                    eMailVerification()
                } else {
                    Log.i(TAG, "User attributes = successfully entered dashboard")
                    viewModelScope.launch {
                        callBackInterface?.callBack(AppConstants.EMAIL_VERIFIED_TRUE_GOTO_DASHBOARD)
                    }
                }
            },
            {
                Log.e(TAG, "Failed to fetch user attributes", it)
                val errMsg = it.cause!!.message!!.split(".")[0]
                Log.e(TAG, "Failed to fetch user attributes $errMsg")
                viewModelScope.launch {
                    if (errMsg.contains(context.resources.getString(R.string.Unable_to_resolve_host)) ||
                        errMsg.contains(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp))
                    ) {
                        callBackInterface!!.awsErrorResponse(context.resources.getString(R.string.Failed_to_connect_to_cognito_idp))
                    } else if (errMsg.contains(context.resources.getString(R.string.Operation_requires_a_signed_in_state))) {
                        callBackInterface!!.awsErrorResponse(context.resources.getString(R.string.Operation_requires_a_signed_in_state))
                    }
//                    else {
//                        toastMessage = AppConstants.INVALID_OTP
//                        callBackInterface!!.awsErrorResponse(num.toString())
//                    }
                }
            })
    }

    // Method For Getting Service Provider Reg Id and Role Id
    fun getLoginInfo(idToken: String) = liveData(Dispatchers.IO) {
        emit(eMailOTPRepository.getLoginInfo(idToken))
    }

    // Method For Getting Service Provider Reg Id and Role Id
    fun getOtpValidation(isValid: Boolean, username: String) = liveData(Dispatchers.IO) {
        emit(eMailOTPRepository.getOtpValidation(isValid, username))
    }

    // Email Verification
    private fun eMailVerification() {
        Amplify.Auth.resendUserAttributeConfirmationCode(AuthUserAttributeKey.email(),
            {
                viewModelScope.launch {
                    callBackInterface?.callBack(AppConstants.EMAIL_VERIFICATION_CODE_PAGE)
                }
            },
            {
                Log.e(TAG, "Failed to resend code", it)
                viewModelScope.launch {
                    val errMsg = it.cause!!.message!!.split(".")[0]
                    toastMessage = errMsg
                    callBackInterface!!.awsErrorResponse(num.toString())
                }
            })
    }

    // 2351 Android-OTP expires Validation
    // OTP Resend SignIn Method
    fun reSendOTP(userName: String, mDataBinding: FragmentEmailOtpBinding) {
        Amplify.Auth.signIn(userName, null, {
            Log.d(TAG, "reSendOTP: called code resented successfully")
            viewModelScope.launch {
                callBackInterface?.callBack(AppConstants.RESEND_OTP)
            }
        },
            {
                Log.e(TAG, "Failed to sign in", it)
                viewModelScope.launch {
                    val errMsg = it.cause!!.message!!.split(".")[0]
                    toastMessage = errMsg
                    callBackInterface!!.awsErrorResponse(num.toString())
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
        fun awsErrorResponse(num: String)
        fun navigatingPage()
        fun showToast(resendRestriction: Int)
        fun otpValidation(b: Boolean)
    }

    // 2351 Android-OTP expires Validation Method
    fun otpTimerValidation(
        mDataBinding: FragmentEmailOtpBinding?, userName: String
    ) {
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
                resendRestriction += 1

                mDataBinding.otpResend.isClickable = true
                countTime.text = AppConstants.INITIAL_TIME

                if (resendRestriction <= 6) {
                    mDataBinding.otpResend.setTextColor(
                        ContextCompat.getColor(
                            getApplication(), R.color.button_blue
                        )
                    )
                    mDataBinding.otpResend.setOnClickListener {
                        if (resendRestriction <= 5) {
                            reSendOTP(userName, mDataBinding)
                            callBackInterface?.showToast(resendRestriction)
                        } else {
                            callBackInterface?.showToast(resendRestriction)
                        }
                    }
                } else {
                    callBackInterface?.showToast(resendRestriction)
                }
            }
        }.start()
    }
}