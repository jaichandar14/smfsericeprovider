package com.smf.events.ui.emailverificationcode

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentEmailVerificationCodeBinding

class EmailVerificationCodeFragment :
    BaseFragment<FragmentEmailVerificationCodeBinding, EmailVerificationCodeViewModel>(),
    EmailVerificationCodeViewModel.CallBackInterface {

    override fun getViewModel(): EmailVerificationCodeViewModel =
        ViewModelProvider(this).get(EmailVerificationCodeViewModel::class.java)

    override fun getBindingVariable(): Int = BR.emailVerificationCodeViewModel

    override fun getContentView(): Int = R.layout.fragment_email_verification_code

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize CallBackInterface
        getViewModel().setCallBackInterface(this)
        init()
    }

    private fun init() {
//        if (checkInternetAvailable()) {
        // Email Verification Code Button Listener
        mDataBinding?.eMailCodesubmitBtn?.setOnClickListener {
            eMailCodeSubmitBtnClicked()
        }

        // Resend Email Verification Text Listener
        mDataBinding?.eMailVerificationCodeResend?.setOnClickListener {
            eMailVerificationCodeResend()
        }
//        }
    }

    // Method for Email Code Verification
    private fun eMailCodeSubmitBtnClicked() {
        var code = mDataBinding?.emailVerificationCode?.text.toString()
        getViewModel().confirmUserAttribute(code)
    }

    // Method for Email verification code Resend
    private fun eMailVerificationCodeResend() {
        getViewModel().resendEmailVerification()
    }

    override fun callBack(status: String) {
        if (status == "EmailVerifiedGoToDashBoard") {
            //Navigate to DashBoardFragment
            findNavController().navigate(EmailVerificationCodeFragmentDirections.actionEmailVerificationCodeFragmentToDashBoardFragment())

        }
    }

    override fun awsErrorResponse() {
        showToast(getViewModel().toastMessage)
    }

}