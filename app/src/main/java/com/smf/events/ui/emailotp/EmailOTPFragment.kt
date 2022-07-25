package com.smf.events.ui.emailotp

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
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
import com.smf.events.ui.splash.SplashFragmentDirections
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

    private var selectedPosition = 0
    lateinit var otp0: EditText
    lateinit var otp1: EditText
    lateinit var otp2: EditText
    lateinit var otp3: EditText
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
        // 2351 Android-OTP expires Validation Method
        getViewModel().otpTimerValidation(mDataBinding, userName)

        otp0 = mDataBinding?.otp1ed!!
        otp1 = mDataBinding?.otp2ed!!
        otp2 = mDataBinding?.otp3ed!!
        otp3 = mDataBinding?.otp4ed!!

        otp0.tag = 0
        otp1.tag = 1
        otp2.tag = 2
        otp3.tag = 3

        otp0.addTextChangedListener(textWatcher)
        otp1.addTextChangedListener(textWatcher)
        otp2.addTextChangedListener(textWatcher)
        otp3.addTextChangedListener(textWatcher)

        otp0.onFocusChangeListener = focusListener
        otp1.onFocusChangeListener = focusListener
        otp2.onFocusChangeListener = focusListener
        otp3.onFocusChangeListener = focusListener

//        otp0.setOnKeyListener(keyListener)
//        otp1.setOnKeyListener(keyListener)
//        otp2.setOnKeyListener(keyListener)
//        otp3.setOnKeyListener(keyListener)

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
        mDataBinding!!.submitBtn.setOnClickListener {
            showProgress()
            getViewModel().confirmSignIn(otp0.text.toString()+otp1.text.toString()
                    +otp2.text.toString()+otp3.text.toString(), mDataBinding!!)
        }
    }

    private fun showProgress() {
        mDataBinding?.textView8?.visibility = View.GONE
        mDataBinding?.textView9?.visibility = View.GONE
        mDataBinding?.textView10?.visibility = View.GONE
        mDataBinding?.otp1ed?.visibility = View.GONE
        mDataBinding?.otp2ed?.visibility = View.GONE
        mDataBinding?.otp3ed?.visibility = View.GONE
        mDataBinding?.otp4ed?.visibility = View.GONE
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
        mDataBinding?.otp1ed?.visibility = View.VISIBLE
        mDataBinding?.otp2ed?.visibility = View.VISIBLE
        mDataBinding?.otp3ed?.visibility = View.VISIBLE
        mDataBinding?.otp4ed?.visibility = View.VISIBLE
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
    override fun awsErrorResponse(num: Int) {
        getOtpValidation(false)
        hideProgress()
        if (num>=4){}else{
        showToast(getViewModel().toastMessage)
        }
        mDataBinding?.otp1ed?.text=null
        mDataBinding?.otp3ed?.text=null
        mDataBinding?.otp2ed?.text=null
        mDataBinding?.otp4ed?.text=null
            }
    // Login api call to Fetch RollId and SpRegId
    private fun getOtpValidation(isValid: Boolean) {
        // Getting Service Provider Reg Id and Role Id
        getViewModel().getOtpValidation(isValid,userName)
            .observe(this@EmailOTPFragment, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        // Initialize RegId And RoleId to Shared Preference
                        //setSpRegIdAndRollID(apiResponse)
                        Log.d(TAG, "getLoginApiCall11: ${apiResponse.response}")
                        // Navigate to DashBoardFragment
                    //   findNavController().navigate(EmailOTPFragmentDirections.actionEMailOTPFragmentToSignInFragment())
                    }
                    is ApisResponse.CustomError -> {
                        Log.d(TAG, "error response: ${apiResponse.message}")
                        // Navigate to DashBoardFragment
                       if ( !apiResponse.message.isNullOrEmpty())
                           showToast(apiResponse.message)
                           findNavController().navigate(EmailOTPFragmentDirections.actionEMailOTPFragmentToSignInFragment())
                       }
                    else -> {
                    }
                }
            })
    }
    // 2351 Android-OTP expires Validation
    override fun navigatingPage() {
        // Navigating from emailOtpScreen to Sign in Screen
        findNavController().navigate(EmailOTPFragmentDirections.actionEMailOTPFragmentToSignInFragment())
    }

    override fun showToast(resendRestriction: Int) {
    if (resendRestriction<=9){
        Toast.makeText(requireContext(),getString(R.string.otp_sent_to_your_mail), Toast.LENGTH_LONG).show()
    }else{
        Toast.makeText(requireContext(),getString(R.string.resend_clicked_multiple_time), Toast.LENGTH_LONG).show()
        val action = EmailOTPFragmentDirections.actionEMailOTPFragmentToSignInFragment()
        findNavController().navigate(action)
    }
    }

    override fun otpValidation() {
            getOtpValidation(true)

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

    private var focusListener = View.OnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
            when (v.tag) {
                0 -> {
                    otp0.setSelectAllOnFocus(true)
                    selectedPosition = 0
                }
                1 -> {
                    otp1.setSelectAllOnFocus(true)
                    selectedPosition = 1
                }
                2 -> {
                    otp2.setSelectAllOnFocus(true)
                    selectedPosition = 2
                }
                3 -> {
                    Log.d("TAG", "called called: ")
                    otp3.setSelectAllOnFocus(true)
                    selectedPosition = 3
                }
            }
        }
    }

    private var textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            Log.d("TAG", "afterTextChanged: called")
            when (selectedPosition) {
                0 -> {
                    if (!s.isNullOrEmpty()) {
                        selectedPosition = 1
                        showKeyboard(otp1)

                        Log.d(TAG, "afterTextChanged: $s")
                    }
                }
                1 -> {
                    if (!s.isNullOrEmpty()) {
                        selectedPosition = 2
                        showKeyboard(otp2)
                        Log.d(TAG, "afterTextChanged: $s")
                    }
                    else{
                        Log.d("TAG", "afterTextChanged: else called")
                        selectedPosition = 1
                        showKeyboard(otp0)
                    }
                }
                2 -> {
                    if (!s.isNullOrEmpty()) {
                        selectedPosition = 3
                        showKeyboard(otp3)
                        Log.d(TAG, "afterTextChanged: $s")
                    }
                    else{
                        Log.d("TAG", "afterTextChanged: else called")
                        selectedPosition = 2
                        showKeyboard(otp1)
                    }
                }
                3 ->{
                    if (!s.isNullOrEmpty()) {
                        Log.d("TAG", "afterTextChanged: else called")
//                        selectedPosition = 3
//                        showKeyboard(otp4)

                    }
                    else{
                        Log.d("TAG", "afterTextChanged: else called")
                        selectedPosition = 3
                        showKeyboard(otp2)
                    }
                }
            }
        }
    }

    private fun showKeyboard(editText: EditText) {
        editText.requestFocus()
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}
