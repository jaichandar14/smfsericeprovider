package com.smf.events.ui.signup

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.FragmentSignupBinding
import com.smf.events.helper.ApisResponse

import com.smf.events.ui.signup.model.UserDetails
import com.smf.events.ui.signup.model.UserDetailsResponse
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class SignUpFragment : BaseFragment<FragmentSignupBinding, SignUpViewModel>(),
    SignUpViewModel.CallBackInterface {

    private var role: String = "EVENT_ORGANIZER"

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): SignUpViewModel? =
        ViewModelProvider(this, factory).get(SignUpViewModel::class.java)

    override fun getBindingVariable(): Int = BR.signupViewModel

    override fun getContentView(): Int = R.layout.fragment_signup

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize CallBackInterface
        getViewModel()?.setCallBackInterface(this)
        setTabLayout()

        // SignUp Button Listener
        mDataBinding!!.submitSignup.setOnClickListener {
            // Method for SignUp Function
            getViewModel()!!.signUpFunctionality(mDataBinding!!,role)
        }
    }

    // SignUp Method Observer
    private val userInfoObserver = Observer<ApisResponse<UserDetailsResponse>> { apiResponse ->
        when (apiResponse) {
            is ApisResponse.Success -> {
                showToast("success")
                Log.d("TAG", "response: success")
            }

            is ApisResponse.CustomError -> {
                showToast(apiResponse.message!!)
                Log.d("TAG", "response: failure ${apiResponse.message}")
            }
        }
    }

    // Method for tabLayout title
    private fun setTabLayout() {
        var roles: String? = "tabSelected"

        mDataBinding!!.tablayout.addTab(
            mDataBinding!!.tablayout.newTab().setText("EVENT_ORGANIZER")
        )
        mDataBinding!!.tablayout.addTab(
            mDataBinding!!.tablayout.newTab().setText("SERVICE_PROVIDER")
        )
        mDataBinding!!.tablayout.tabGravity = TabLayout.GRAVITY_FILL
        mDataBinding!!.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                roles = tab?.text.toString()
                Log.d("TAG", "onTabSelected: roles")
                tabSelected(roles!!)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

    }

    fun tabSelected(roles: String) {
        Log.d("TAG", "tabSelected: $roles")
        role = roles

    }

    // CallBackInterface Override Method
    override fun callBack(
        status: String,
        message: String?,
        userDetails: UserDetails,
        userName: String
    ) {

        if (status == "signUpResult") {
            //Api post call for userDetails
            getViewModel()?.setUserDetails(userDetails)
                ?.observe(viewLifecycleOwner, userInfoObserver)
            val action =
                SignUpFragmentDirections.actionSignUpFragmentToVerificationCodeFrgment(userName)
            findNavController().navigate(action)

        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun authResponse() {
        getViewModel()?.toastMessage?.let { showToast(it) }
    }

}