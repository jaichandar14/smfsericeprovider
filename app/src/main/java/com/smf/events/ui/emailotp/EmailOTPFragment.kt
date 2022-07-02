package com.smf.events.ui.emailotp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentEmailOtpBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.ui.emailotp.model.GetLoginInfo
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.*
import javax.inject.Inject

class EmailOTPFragment : BaseFragment<FragmentEmailOtpBinding, EmailOTPViewModel>(),
    EmailOTPViewModel.CallBackInterface {

    private val TAG = "EmailOTPFragment"
    private val args: EmailOTPFragmentArgs by navArgs()
    private lateinit var userName: String
    private lateinit var firstName: String
    private lateinit var emailId: String

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    override fun getViewModel(): EmailOTPViewModel =
        ViewModelProvider(this, factory).get(EmailOTPViewModel::class.java)

    override fun getBindingVariable(): Int = BR.otpviewmodel

    override fun getContentView(): Int = R.layout.fragment_email_otp

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Local Variables
        setUserNameAndSharedPref()
        // Initialize CallBackInterface
        getViewModel().setCallBackInterface(this)
        // Submit Button Listener
        submitBtnClicked()
        // Auto submit pin
        autoSubmit()
        // 2351 Android-OTP expires Validation Method
        getViewModel().otpTimerValidation(mDataBinding, userName)
    }

    // Method For set UserName And SharedPreferences
    private fun setUserNameAndSharedPref() {
        userName = args.userName
        sharedPreference.putString(
            SharedPreference.FIRST_NAME,
            args.firstName
        )
        sharedPreference.putString(
            SharedPreference.EMAIL_ID,
            args.emailId
        )
    }

    // For confirmSignIn aws
    private fun submitBtnClicked() {
        val code = mDataBinding?.otpemail?.text.toString()
        mDataBinding!!.submitBtn.setOnClickListener {
            getViewModel().confirmSignIn(code, mDataBinding!!)
        }
    }

    // AutoSubmitted when we Enter 4 digit
    private fun autoSubmit() {
        getViewModel().userOtpNumber.observe(viewLifecycleOwner, Observer {
            if (it.length == 4) {
                showProgress()
                getViewModel().confirmSignIn(it, mDataBinding!!)
            } else {
                hideProgress()
            }
        })
    }

    private fun showProgress() {
        mDataBinding?.textView8?.visibility = View.GONE
        mDataBinding?.textView9?.visibility = View.GONE
        mDataBinding?.textView10?.visibility = View.GONE
        mDataBinding?.otpemail?.visibility = View.GONE
        mDataBinding?.linearLayout4?.visibility = View.GONE
        mDataBinding?.textView11?.visibility = View.GONE
        mDataBinding?.otpResend?.visibility = View.GONE
        mDataBinding?.submitBtn?.visibility = View.GONE
        mDataBinding?.progressBar?.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        mDataBinding?.textView8?.visibility = View.VISIBLE
        mDataBinding?.textView9?.visibility = View.VISIBLE
        mDataBinding?.textView10?.visibility = View.VISIBLE
        mDataBinding?.otpemail?.visibility = View.VISIBLE
        mDataBinding?.linearLayout4?.visibility = View.VISIBLE
        mDataBinding?.textView11?.visibility = View.VISIBLE
        mDataBinding?.otpResend?.visibility = View.VISIBLE
        mDataBinding?.submitBtn?.visibility = View.VISIBLE
        mDataBinding?.progressBar?.visibility = View.INVISIBLE
    }

    // CallBackInterface Override Method
    override suspend fun callBack(status: String) {
        if (status == AppConstants.EMAIL_VERIFICATION_CODE_PAGE) {
            // Navigate to EmailVerificationCodeFragment
            findNavController().navigate(EmailOTPFragmentDirections.actionPhoneOTPFragmentToEmailVerificationCodeFragment())
        } else if (status == AppConstants.EMAIL_VERIFIED_TRUE_GOTO_DASHBOARD) {
            val idToken =
                "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
            getLoginApiCall(idToken)
        }
    }

    // AWS Error response
    override fun awsErrorResponse() {
        hideProgress()
        showToast(getViewModel().toastMessage)
    }

    // 2351 Android-OTP expires Validation
    override fun navigatingPage() {
        // Navigating from emailOtpScreen to Sign in Screen
        findNavController().navigate(EmailOTPFragmentDirections.actionEMailOTPFragmentToSignInFragment())
    }

    // Login api call to Fetch RollId and SpRegId
    private fun getLoginApiCall(idToken: String) {
        // Getting Service Provider Reg Id and Role Id
        getViewModel().getLoginInfo(idToken)
            .observe(this@EmailOTPFragment, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        // Initialize RegId And RoleId to Shared Preference
                        setSpRegIdAndRollID(apiResponse)
                        Log.d(TAG, "getLoginApiCall: $apiResponse")
                        // Navigate to DashBoardFragment
                        findNavController().navigate(EmailOTPFragmentDirections.actionEMailOTPFragmentToDashBoardFragment())
                    }
                    is ApisResponse.Error -> {
                        Log.d(TAG, "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
    }

    // Method For Set SpRegId And RollID to SharedPreference From Login Api
    private fun setSpRegIdAndRollID(apiResponse: ApisResponse.Success<GetLoginInfo>) {
        sharedPreference.putInt(
            SharedPreference.SP_REG_ID,
            apiResponse.response.data.spRegId
        )
        sharedPreference.putInt(
            SharedPreference.ROLE_ID,
            apiResponse.response.data.roleId
        )
    }
}
