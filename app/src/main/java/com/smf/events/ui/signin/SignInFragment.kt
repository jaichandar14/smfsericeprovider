package com.smf.events.ui.signin

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.SignInFragmentBinding
import com.smf.events.helper.ApisResponse
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
        // Initialize CallBackInterface
        getViewModel().setCallBackInterface(this)
        // SignIn Button Listener
        signInClicked()
        // SignUp Button Listener
        onSignUpClicked()
    }

    // Method for SignIn Button
    private fun signInClicked() {
        mDataBinding!!.signinbtn.setOnClickListener {
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
                        getViewModel().getUserDetails(eMail)
                            .observe(viewLifecycleOwner, getUserDetailsObserver)
                    } else {
                        showProgress()
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
                getViewModel().signIn(apiResponse.response.data.userName)
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
        mDataBinding!!.signupaccbtn.setOnClickListener {
            val action = SignInFragmentDirections.actionSignInFragmentToSignUpFragment()
            // Navigate to SignUpFragment
            findNavController().navigate(action)
        }
    }

    // CallBackInterface Override Method
    override fun callBack(status: String) {
        when (status) {
            "SignInNotCompleted" -> {
                // Navigate to EmailOTPFragment
                // 2842 Hiding the login page
                //  mDataBinding?.loginPage?.visibility=View.VISIBLE
                findNavController().navigate(
                    SignInFragmentDirections.actionSignInFragmentToEMailOTPFragment(
                        userName!!
                    )
                )
                mDataBinding?.progressBar?.visibility = View.GONE
            }
            "signInCompletedGoToDashBoard" -> {
                //Navigate to DashBoardFragment
                findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToDashBoardFragment())
            }
            "resend success" -> {
                //Navigate to MobileVerificationCode
                findNavController().navigate(
                    SignInFragmentDirections.actionSignInFragmentToVerificationCodeFrgment(
                        userName!!
                    )
                )
            }
            "resend failure" -> {

            }
        }
    }

    override fun awsErrorResponse() {
        showToast(getViewModel().toastMessage)
    }

}