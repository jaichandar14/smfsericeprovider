package com.smf.events.ui.signin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.smf.events.BR
import com.smf.events.MainActivity
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.SignInFragmentBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.signup.model.GetUserDetails
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

class SignInFragment : BaseFragment<SignInFragmentBinding, SignInViewModel>(),
    SignInViewModel.CallBackInterface {

    var TAG = this::class.java.name
    private lateinit var mobileNumberWithCountryCode: String
    private lateinit var encodedMobileNo: String
    private lateinit var eMail: String
    private var userName: String? = null
    private var firstName: String? = null
    private var emailId: String? = null
    private lateinit var constraintLayout: ConstraintLayout
    private var userInfo: String = ""
    lateinit var dialogDisposable: Disposable

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    lateinit var firebaseAnalytics: FirebaseAnalytics
    override fun getViewModel(): SignInViewModel =
        ViewModelProvider(this, factory)[SignInViewModel::class.java]

    override fun getBindingVariable(): Int = BR.signinViewModel

    override fun getContentView(): Int = R.layout.sign_in_fragment

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set Status bar
        (requireActivity() as MainActivity).setStatusBarColor()
        firebaseAnalytics = Firebase.analytics
        //restrict user back button
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(
            TAG,
            "TokenExpiry signin ${sharedPreference.getString(SharedPreference.ID_Token)}"
        )
        constraintLayout = mDataBinding?.loginPage!!
        // Initialize CallBackInterface
        getViewModel().setCallBackInterface(this)
        init()
    }

    override fun onResume() {
        super.onResume()
        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer sign frag called")
        }
    }

    private fun init() {
        // SignIn Button Listener
        signInClicked()
        // SignUp Button Listener
        onSignUpClicked()
        // Hiding Error Message Listeners
        errorMessageListeners()
    }

    private fun errorMessageListeners() {
        getViewModel().getMobileNumber.observe(viewLifecycleOwner, Observer {
            if (mDataBinding?.loginMessageText?.isVisible == true || mDataBinding?.loginEmailMessageText?.isVisible == true) {
                mDataBinding?.loginMessageText?.visibility = View.GONE
                mDataBinding?.loginEmailMessageText?.visibility = View.GONE
            }
        })

        getViewModel().getEmailId.observe(viewLifecycleOwner, Observer {
            if (mDataBinding?.loginMessageText?.isVisible == true || mDataBinding?.loginEmailMessageText?.isVisible == true) {
                mDataBinding?.loginMessageText?.visibility = View.GONE
                mDataBinding?.loginEmailMessageText?.visibility = View.GONE
            }
        })
    }

    // 3028 on Close Sign out
    private fun signOut() {
        Amplify.Auth.signOut(
            AuthSignOutOptions.builder().globalSignOut(true).build(),
            {
                CoroutineScope(Dispatchers.Main).launch {
                    afterSignOut()
                }
            },
            {
                CoroutineScope(Dispatchers.Main).launch {
                    afterSignOut()
                }
                Log.e("AuthQuickstart", "Sign out failed", it)
            }
        )
    }

    // Method for SignIn Button
    private fun signInClicked() {
        mDataBinding?.signinbtn?.setOnClickListener {
            if (getViewModel().getProgressValue().not()) {
                getViewModel().showProgress()
                signOut()
            }
        }
    }

    private fun afterSignOut() {
        mDataBinding?.loginMessageText?.visibility = View.GONE
        mDataBinding?.loginEmailMessageText?.visibility = View.GONE
        // 2845 - Hiding Progress Bar
        hideKeyBoard()
        val phoneNumber = mDataBinding?.editTextMobileNumber?.text.toString().trim()
        val countryCode = mDataBinding?.cppSignIn?.selectedCountryCode
        mobileNumberWithCountryCode = "+".plus(countryCode).plus(phoneNumber)

        // Single Encoding
        encodedMobileNo = URLEncoder.encode(mobileNumberWithCountryCode, "UTF-8")
        eMail = mDataBinding?.editTextEmail?.text.toString()
        // Sedding log event to the adb shell setprop debug.firebase.analytics.app com.smf.events.qafirebase
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.METHOD, eMail)
        firebaseAnalytics.logEvent("userdetails") {
            param("email", eMail)
            param("mobileen", encodedMobileNo)
        }
        firebaseAnalytics.setUserProperty("userId", userName)
        // Validate Mail and phone number
        emailAndNumberValidation(phoneNumber, eMail)
    }

    fun emailAndNumberValidation(phoneNumber: String, eMail: String): Boolean {
        if (phoneNumber.isNotEmpty() || eMail.isNotEmpty()) {
            if (phoneNumber.isEmpty() || eMail.isEmpty()) {
                return if (phoneNumber.isEmpty()) {
                    userInfo = AppConstants.EMAIL
                    if (::factory.isInitialized) {
                        getViewModel().getUserDetails(eMail)
                            .observe(viewLifecycleOwner, getUserDetailsObserver)
                    }
                    true
                } else {
                    userInfo = AppConstants.MOBILE
                    if (::factory.isInitialized) {
                        getViewModel().getUserDetails(encodedMobileNo)
                            .observe(viewLifecycleOwner, getUserDetailsObserver)
                    }
                    true
                }
            } else {
                if (::factory.isInitialized) {
                    getViewModel().hideProgress()
                }
                if (context != null) {
                    showToastMessage(
                        resources.getString(R.string.Please_Enter_Any_EMail_or_Phone_Number),
                        Snackbar.LENGTH_LONG,
                        AppConstants.PLAIN_SNACK_BAR
                    )
                    firebaseAnalytics.logEvent("SignInError") {
                        param(
                            "Enter any one Id Error",
                            resources.getString(R.string.Please_Enter_Any_EMail_or_Phone_Number)
                        )
                    }
                }
                return false
            }
        } else {
            getViewModel().hideProgress()
            if (context != null) {
                showToastMessage(
                    resources.getString(R.string.Please_Enter_Email_or_MobileNumber),
                    Snackbar.LENGTH_LONG,
                    AppConstants.PLAIN_SNACK_BAR
                )
                firebaseAnalytics.logEvent("SignInError") {
                    param(
                        "Not entered login Id error",
                        resources.getString(R.string.Please_Enter_Email_or_MobileNumber)
                    )
                }
            }
        }
        return false
    }

    // Observing GetUserDetails Api Call
    private val getUserDetailsObserver = Observer<ApisResponse<GetUserDetails>> { apiResponse ->
        when (apiResponse) {
            is ApisResponse.Success -> {
                userName = apiResponse.response.data.userName
                firstName = apiResponse.response.data.firstName
                emailId = apiResponse.response.data.email
                if (AppConstants.SERVICE_PROVIDER == apiResponse.response.data.role) {
                    getViewModel().signIn(apiResponse.response.data.userName, requireContext())
                    firebaseAnalytics.setUserId(apiResponse.response.data.userName)
                } else {
                    getViewModel().hideProgress()
                    if (userInfo == AppConstants.EMAIL) {
                        mDataBinding?.loginEmailMessageText?.visibility = View.VISIBLE
                    } else {
                        mDataBinding?.loginMessageText?.visibility = View.VISIBLE
                    }
                }
            }
            is ApisResponse.CustomError -> {
                getViewModel().hideProgress()
                showToast(apiResponse.message)
            }
            is ApisResponse.InternetError -> {
                (requireActivity() as MainActivity).showInternetDialog(apiResponse.message)
                getViewModel().hideProgress()
            }
            else -> {}
        }
    }

    // Method for SignUp Button
    private fun onSignUpClicked() {
//        mDataBinding!!.signupaccbtn.setOnClickListener {
//            val action = SignInFragmentDirections.actionSignInFragmentToSignUpFragment()
//            // Navigate to SignUpFragment
//            findNavController().navigate(action)
//        }
    }

    // CallBackInterface Override Method
    override fun callBack(status: String) {
        when (status) {
            AppConstants.SIGN_IN_NOT_COMPLETED -> {
                // Navigate to EmailOTPFragment
                // 2842 Hiding the login page
                //  mDataBinding?.loginPage?.visibility=View.VISIBLE
                findNavController().navigate(
                    SignInFragmentDirections.actionSignInFragmentToEMailOTPFragment(
                        userName!!, firstName!!, emailId!!
                    )
                )
                getViewModel().showLoading.value = false
            }
            AppConstants.SIGN_IN_COMPLETED_GOTO_DASH_BOARD -> {
                Log.i(TAG, "Sign in succeeded frag")
                signOut()
//                //Navigate to DashBoardFragment
//                findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToDashBoardFragment())
            }
            AppConstants.RESEND_SUCCESS -> {
                //Navigate to MobileVerificationCode
                findNavController().navigate(
                    SignInFragmentDirections.actionSignInFragmentToVerificationCodeFrgment(
                        userName!!
                    )
                )
            }
            AppConstants.RESEND_FAILURE -> {

            }
        }
    }

    override fun awsErrorResponse(message: String) {
        if (message == resources.getString(R.string.Failed_to_connect_to_cognito_idp)) {
            (requireActivity() as MainActivity).showInternetDialog(AppConstants.SHOW_INTERNET_DIALOG)
        } else {
            showToast(getViewModel().toastMessage)
        }
        getViewModel().hideProgress()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: called signin frag")
        if (dialogDisposable.isDisposed.not()) dialogDisposable.dispose()
        getViewModel().toastMessageG.value?.msg = ""
    }
}