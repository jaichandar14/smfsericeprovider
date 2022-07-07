package com.smf.events.ui.signin

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.SignInFragmentBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.ui.signup.model.GetUserDetails
import dagger.android.support.AndroidSupportInjection
import java.net.URLEncoder
import javax.inject.Inject

class SignInFragment : BaseFragment<SignInFragmentBinding, SignInViewModel>(),
    SignInViewModel.CallBackInterface {

    private lateinit var mobileNumberWithCountryCode: String
    private lateinit var encodedMobileNo: String
    private lateinit var eMail: String
    private var userName: String? = null
    private var firstName: String? = null
    private var emailId: String? = null
    private lateinit var constraintLayout: ConstraintLayout
    private var userInfo: String = ""

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): SignInViewModel =
        ViewModelProvider(this, factory).get(SignInViewModel::class.java)

    override fun getBindingVariable(): Int = BR.signinViewModel

    override fun getContentView(): Int = R.layout.sign_in_fragment

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //restrict user back button
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        constraintLayout = mDataBinding?.loginPage!!
        // Initialize CallBackInterface
        getViewModel().setCallBackInterface(this)
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

    // Method for SignIn Button
    private fun signInClicked() {
        mDataBinding!!.signinbtn.setOnClickListener {
            mDataBinding?.loginMessageText?.visibility = View.GONE
            mDataBinding?.loginEmailMessageText?.visibility = View.GONE
            // 2845 - Hiding Progress Bar
            hideKeyBoard()
            val phoneNumber = mDataBinding?.editTextMobileNumber?.text.toString().trim()
            val countryCode = mDataBinding?.cppSignIn?.selectedCountryCode
            mobileNumberWithCountryCode = "+".plus(countryCode).plus(phoneNumber)

            //SingleEncoding
            encodedMobileNo = URLEncoder.encode(mobileNumberWithCountryCode, "UTF-8")
            eMail = mDataBinding?.editTextEmail?.text.toString()

            if (phoneNumber.isNotEmpty() || eMail.isNotEmpty()) {
                if (phoneNumber.isEmpty() || eMail.isEmpty()) {
                    if (phoneNumber.isEmpty()) {
                        showProgress()
                        userInfo = AppConstants.EMAIL
                        getViewModel().getUserDetails(eMail)
                            .observe(viewLifecycleOwner, getUserDetailsObserver)
                    } else {
                        showProgress()
                        userInfo = AppConstants.MOBILE
                        getViewModel().getUserDetails(encodedMobileNo)
                            .observe(viewLifecycleOwner, getUserDetailsObserver)
                    }
                } else {
                    showToast("Please Enter Any EMail or Phone Number")
                }
            } else {
                showToast("Please Enter Email or MobileNumber")
            }
        }
    }

    // Observing GetUserDetails Api Call
    private val getUserDetailsObserver = Observer<ApisResponse<GetUserDetails>> { apiResponse ->
        when (apiResponse) {
            is ApisResponse.Success -> {
                userName = apiResponse.response.data.userName
                firstName = apiResponse.response.data.firstName
                emailId = apiResponse.response.data.email
                if (AppConstants.SERVICE_PROVIDER == apiResponse.response.data.role) {
                    getViewModel().signIn(apiResponse.response.data.userName)
                } else {
                    hideProgress()
                    if (userInfo == AppConstants.EMAIL) {
                        mDataBinding?.loginEmailMessageText?.visibility = View.VISIBLE
                    } else {
                        mDataBinding?.loginMessageText?.visibility = View.VISIBLE
                    }

                }
            }
            is ApisResponse.CustomError -> {
                hideProgress()
                showToast(apiResponse.message)
            }
            else -> {
            }
        }
    }

    private fun showProgress() {
        mDataBinding?.textView4?.visibility = View.GONE
        mDataBinding?.textView5?.visibility = View.GONE
        mDataBinding?.phnumerlayout?.visibility = View.GONE
        mDataBinding?.textView7?.visibility = View.GONE
        mDataBinding?.textView6?.visibility = View.GONE
        mDataBinding?.mailidLayout?.visibility = View.GONE
        mDataBinding?.signinbtn?.visibility = View.GONE
        mDataBinding?.progressBar?.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        mDataBinding?.textView4?.visibility = View.VISIBLE
        mDataBinding?.textView5?.visibility = View.VISIBLE
        mDataBinding?.phnumerlayout?.visibility = View.VISIBLE
        mDataBinding?.textView7?.visibility = View.VISIBLE
        mDataBinding?.textView6?.visibility = View.VISIBLE
        mDataBinding?.mailidLayout?.visibility = View.VISIBLE
        mDataBinding?.signinbtn?.visibility = View.VISIBLE
        mDataBinding?.progressBar?.visibility = View.GONE
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
                mDataBinding?.progressBar?.visibility = View.GONE
            }
            AppConstants.SIGN_IN_COMPLETED_GOTO_DASH_BOARD -> {
                //Navigate to DashBoardFragment
                findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToDashBoardFragment())
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

    override fun awsErrorResponse() {
        showToast(getViewModel().toastMessage)
    }

}