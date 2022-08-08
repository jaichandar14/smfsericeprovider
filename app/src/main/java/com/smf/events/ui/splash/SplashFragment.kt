package com.smf.events.ui.splash

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.core.Amplify
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.SplashScreenFragmentBinding
import com.smf.events.helper.SharedPreference
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SplashFragment : BaseFragment<SplashScreenFragmentBinding, SplashScreenViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    lateinit var idToken: String

    @Inject
    lateinit var sharedPreference: SharedPreference

    override fun getViewModel(): SplashScreenViewModel =
        ViewModelProvider(this, factory).get(SplashScreenViewModel::class.java)

    override fun getBindingVariable(): Int = BR.splashViewModel

    override fun getContentView(): Int = R.layout.splash_screen_fragment

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setIdToken()
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Splash Button Listener
        onClickSplashScreenBtn()
    }

    // Sign In Button
    private fun onClickSplashScreenBtn() {
        mDataBinding!!.splashBtn.setOnClickListener {
            val action = SplashFragmentDirections.actionSplashFragmentToSignInFragment()
            findNavController().navigate(action)
        }
    }

    // Method for Moving DashBoard Screen
    private fun moveToDashBoardScreen() {
        val action = SplashFragmentDirections.actionSplashFragmentToDashBoardFragment()
        findNavController().navigate(action)
    }

    private fun setIdToken() {
        idToken = "${sharedPreference.getString(SharedPreference.ID_Token)}"
    }
}