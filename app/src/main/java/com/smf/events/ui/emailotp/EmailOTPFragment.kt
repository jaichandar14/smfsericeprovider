package com.smf.events.ui.emailotp

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.smf.events.BR
import com.smf.events.MainActivity
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentEmailOtpBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.InternetErrorDialog
import com.smf.events.helper.SharedPreference
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.emailotp.model.GetLoginInfo
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class EmailOTPFragment : BaseFragment<FragmentEmailOtpBinding, EmailOTPViewModel>(),
    EmailOTPViewModel.CallBackInterface {

    private val TAG = "EmailOTPFragment"
    private val args: EmailOTPFragmentArgs by navArgs()
    private lateinit var userName: String
    lateinit var otp0: EditText
    lateinit var otp1: EditText
    lateinit var otp2: EditText
    lateinit var otp3: EditText
    private lateinit var internetErrorDialog: InternetErrorDialog
    lateinit var dialogDisposable: Disposable
    var isInternetError = false

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
        // Set Status bar
        (requireActivity() as MainActivity).setStatusBarColor()
        // Initialize Local Variables
        setUserNameAndSharedPref()
        internetErrorDialog = InternetErrorDialog.newInstance()
        // Initialize CallBackInterface
        getViewModel().setCallBackInterface(this)
        // 2351 Android-OTP expires Validation Method
        getViewModel().otpTimerValidation(
            mDataBinding,
            userName,
            internetErrorDialog,
            requireContext()
        )

        otp0 = mDataBinding?.otp1ed!!
        otp1 = mDataBinding?.otp2ed!!
        otp2 = mDataBinding?.otp3ed!!
        otp3 = mDataBinding?.otp4ed!!

        otp0.tag = 0
        otp1.tag = 1
        otp2.tag = 2
        otp3.tag = 3

        //GenericTextWatcher here works only for moving to next EditText when a number is entered
        //first parameter is the current EditText and second parameter is next EditText
        otp0.addTextChangedListener(GenericTextWatcher(otp0, otp1))
        otp1.addTextChangedListener(GenericTextWatcher(otp1, otp2))
        otp2.addTextChangedListener(GenericTextWatcher(otp2, otp3))
        otp3.addTextChangedListener(GenericTextWatcher(otp3, null))

        //GenericKeyEvent here works for deleting the element and to switch back to previous EditText
        //first parameter is the current EditText and second parameter is previous EditText
        otp0.setOnKeyListener(GenericKeyEvent(otp0, null))
        otp1.setOnKeyListener(GenericKeyEvent(otp1, otp0))
        otp2.setOnKeyListener(GenericKeyEvent(otp2, otp1))
        otp3.setOnKeyListener(GenericKeyEvent(otp3, otp2))
        // Method for submit event implementation
        submitBtnClicked()
    }

    override fun onResume() {
        super.onResume()
        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer email otp")
            internetErrorDialog.dismissDialog()
            if (isInternetError) {
                moveToSignIn()
            }
        }
    }

    private fun moveToSignIn() {
        //Navigate to SignInFragment
        findNavController().navigate(EmailOTPFragmentDirections.actionEMailOTPFragmentToSignInFragment())
        isInternetError = false
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
            if (internetErrorDialog.checkInternetAvailable(requireContext())) {
                otpValidation(
                    otp0.text.toString(), otp1.text.toString(),
                    otp2.text.toString(), otp3.text.toString()
                )
            }
        }
    }

    fun otpValidation(
        otp0: String,
        otp1: String,
        otp2: String,
        otp3: String
    ): Boolean {
        return if (otp0.isEmpty()) {
            if (::factory.isInitialized) {
                showToastMessage(
                    AppConstants.ENTER_OTP,
                    Snackbar.LENGTH_SHORT, AppConstants.PLAIN_SNACK_BAR
                )
            }
            false
        } else {
            showProgress()
            if (::factory.isInitialized) {
                Log.d(TAG, "submitBtnClicked: ${otp0 + otp1 + otp2 + otp3}")
                getViewModel().confirmSignIn(
                    requireContext(),
                    otp0 + otp1 + otp2 + otp3, mDataBinding!!
                )
            }
            true
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
            getOtpValidation(true)
        } else if (status == AppConstants.RESEND_OTP) {
            getViewModel().otpTimerValidation(
                mDataBinding,
                userName,
                internetErrorDialog,
                requireContext()
            )
        }
    }

    // AWS Error response
    override fun awsErrorResponse(num: String) {
        if (num == resources.getString(R.string.Failed_to_connect_to_cognito_idp)) {
            Log.e(TAG, "Failed to fetch user attributes inside")
            SharedPreference.isInternetConnected = false
            isInternetError = true
            internetErrorDialog.checkInternetAvailable(requireContext())
        } else if (num == resources.getString(R.string.Operation_requires_a_signed_in_state)) {
            SharedPreference.isInternetConnected = false
            isInternetError = true
            internetErrorDialog.checkInternetAvailable(requireContext())
            showToastMessage(
                resources.getString(R.string.Network_Error__Please_Sign_in_again),
                Snackbar.LENGTH_LONG, AppConstants.PLAIN_SNACK_BAR
            )
        } else {
            showProgress()
            getOtpValidation(false)
            if (num.toInt() >= 3) {
            } else {
                showToastMessage(
                    getViewModel().toastMessage,
                    Snackbar.LENGTH_LONG, AppConstants.PLAIN_SNACK_BAR
                )
            }
            mDataBinding?.otp1ed?.text = null
            mDataBinding?.otp3ed?.text = null
            mDataBinding?.otp2ed?.text = null
            mDataBinding?.otp4ed?.text = null
        }
    }

    // Login api call to Fetch RollId and SpRegId
    private fun getOtpValidation(isValid: Boolean) {
        Log.d(TAG, "getLoginApiCallaaa: getOtpValidation called")
        // Getting Service Provider Reg Id and Role Id
        getViewModel().getOtpValidation(isValid, userName)
            .observe(this@EmailOTPFragment, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        Log.d(TAG, "getLoginApiCallaaa: getOtpValidation scess called")
                        // Initialize RegId And RoleId to Shared Preference
                        //setSpRegIdAndRollID(apiResponse)
                        Log.d(TAG, "getLoginApiCall11: ${apiResponse.response}")
                        // Navigate to DashBoardFragment
                        if (isValid) {
                            Log.d(TAG, "getLoginApiCallaaa: getOtpValidation isvalid called")
                            showProgress()
                            CoroutineScope(Dispatchers.Main).launch {
                                getLoginApiCall(
                                    "${AppConstants.BEARER} ${
                                        sharedPreference.getString(
                                            SharedPreference.ID_Token
                                        )
                                    }"
                                )
                            }
                        } else {
                            hideProgress()
                        }
                    }
                    is ApisResponse.CustomError -> {
                        Log.d(TAG, "error response: ${apiResponse.message}")
                        // Navigate to DashBoardFragment
                        showProgress()
                        if (!apiResponse.message.isNullOrEmpty()) {
                            showToastMessage(
                                apiResponse.message,
                                Snackbar.LENGTH_LONG, AppConstants.PLAIN_SNACK_BAR
                            )
                            findNavController().navigate(EmailOTPFragmentDirections.actionEMailOTPFragmentToSignInFragment())
                        }
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
        if (resendRestriction <= 5) {
            showToastMessage(
                getString(R.string.otp_sent_to_your_mail),
                Snackbar.LENGTH_LONG, AppConstants.PLAIN_SNACK_BAR
            )
        } else {
            showToastMessage(
                getString(R.string.resend_clicked_multiple_time),
                Snackbar.LENGTH_LONG, AppConstants.PLAIN_SNACK_BAR
            )
            val action = EmailOTPFragmentDirections.actionEMailOTPFragmentToSignInFragment()
            findNavController().navigate(action)
        }
    }

    override fun otpValidation(b: Boolean) {
        hideProgress()
        showToastMessage(
            AppConstants.ENTER_OTP,
            Snackbar.LENGTH_SHORT, AppConstants.PLAIN_SNACK_BAR
        )
    }

    override fun internetError(exception: String) {
        Log.d(InternetErrorDialog.TAG, "onViewCreated: observer otp internetError called")
        SharedPreference.isInternetConnected = false
        isInternetError = true
        internetErrorDialog.checkInternetAvailable(requireContext())
    }

    // Login api call to Fetch RollId and SpRegId
    private fun getLoginApiCall(idToken: String) {
        showProgress()
        getViewModel().getLoginInfo(idToken)
            .observe(this@EmailOTPFragment, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        // Initialize RegId And RoleId to Shared Preference
                        setSpRegIdAndRollID(apiResponse)
                        // Navigate to DashBoardFragment
                        findNavController().navigate(
                            EmailOTPFragmentDirections.actionEMailOTPFragmentToDashBoardFragment(
                                AppConstants.TRUE
                            )
                        )
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
        sharedPreference.putString(
            SharedPreference.USER_ID,
            apiResponse.response.data.userName
        )
    }

    class GenericKeyEvent internal constructor(
        private val currentView: EditText,
        private val previousView: EditText?,
    ) : View.OnKeyListener {
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.otp1ed && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }
    }

    class GenericTextWatcher internal constructor(
        private val currentView: View,
        private val nextView: View?,
    ) : TextWatcher {
        override fun afterTextChanged(editable: Editable) { // TODO Auto-generated method stub
            val text = editable.toString()
            when (currentView.id) {
                R.id.otp1ed -> if (text.length == 1) nextView!!.requestFocus()
                R.id.otp2ed -> if (text.length == 1) nextView!!.requestFocus()
                R.id.otp3ed -> if (text.length == 1) nextView!!.requestFocus()
//                R.id.otp4ed -> if (text.length == 1) nextView!!.requestFocus()
                //You can use EditText4 same as above to hide the keyboard
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int,
        ) { // TODO Auto-generated method stub
        }

        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int,
        ) { // TODO Auto-generated method stub
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called email otp")
        if (!dialogDisposable.isDisposed) dialogDisposable.dispose()
    }
}