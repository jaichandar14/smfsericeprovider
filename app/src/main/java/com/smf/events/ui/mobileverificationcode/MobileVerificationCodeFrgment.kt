package com.smf.events.ui.mobileverificationcode

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentMobileVerificationCodeFrgmentBinding

class MobileVerificationCodeFrgment :
    BaseFragment<FragmentMobileVerificationCodeFrgmentBinding, MobileVerificationCodeViewModel>(),
    MobileVerificationCodeViewModel.CallBackInterface {

    private lateinit var userName: String
    private val args: MobileVerificationCodeFrgmentArgs by navArgs()

    override fun getViewModel(): MobileVerificationCodeViewModel =
        ViewModelProvider(this).get(MobileVerificationCodeViewModel::class.java)

    override fun getBindingVariable(): Int = BR.verificationCodeViewModel

    override fun getContentView(): Int = R.layout.fragment_mobile_verification_code_frgment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userName = args.userName

        // Initialize CallBackInterface
        getViewModel().setCallBackInterface(this)

        init()
    }

    private fun init() {
//        if (checkInternetAvailable()) {
        // VerificationCode code Submit Button Listener
        mDataBinding?.submitBtn?.setOnClickListener {
            val code = mDataBinding?.verificationCode?.text.toString()

            if (code.isEmpty() || code.length < 6) {
                Toast.makeText(requireContext(), "Enter Valid Code", Toast.LENGTH_SHORT).show()
            } else {
                confirmSignUpFunctionality(code)
            }
        }

        // ResendVerificationCode TextView Listener
        mDataBinding?.codeResend?.setOnClickListener {
            resendCodeClicked()
        }
//        }
    }

    // Resend Code Function
    private fun resendCodeClicked() {
        getViewModel().resendSignUp(userName)
    }

    // Method for confirmSignUp
    private fun confirmSignUpFunctionality(code: String) {
        getViewModel().confirmSignUpFunctionality(userName, code)
    }

    // CallBackInterface callBack Method
    override fun callBack(status: String, flag: String) {
        if (flag == "SignUpCompleted") {
            Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
            findNavController().navigate(MobileVerificationCodeFrgmentDirections.actionVerificationCodeFrgmentToSignInFragment())
        } else {
            Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
        }

    }

    override fun awsErrorResponse() {
        getViewModel().toastMessage.let { showToast(it) }
    }


}