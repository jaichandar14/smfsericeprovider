package com.smf.events.ui.splash

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.base.BaseFragment
import com.smf.events.databinding.SplashScreenFragmentBinding
import com.smf.events.helper.Analytics
import com.smf.events.helper.AppConstants
import com.smf.events.helper.ApplicationUtils
import com.smf.events.helper.SharedPreference
import com.smf.events.ui.notification.model.NotificationParams
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SplashFragment : BaseFragment<SplashScreenFragmentBinding, SplashScreenViewModel>() {

    var TAG = "SplashFragment"
    lateinit var idToken: String

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference
    lateinit var firebaseAnalytics: FirebaseAnalytics
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
        firebaseAnalytics = Firebase.analytics
        setIdToken()
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 3103
        val notificationParams: NotificationParams? =
            requireActivity().intent.getParcelableExtra(AppConstants.NOTIFICATION_PARAMS)
        // 3103 - Condition For Redirect Notification To DashBoard
        if (notificationParams?.fromNotification == true) {
            ApplicationUtils.fromNotification = notificationParams.fromNotification
            moveToDashBoardScreen()
        } else if (ApplicationUtils.backArrowNotification) {
            // ReUpdate Status for back arrow
            ApplicationUtils.backArrowNotification = false
            moveToDashBoardScreen()
        } else {
            // Splash Button Listener
            onClickSplashScreenBtn()
        }
        Log.d(TAG, "init: ${notificationParams?.fromNotification}")
    }

    // Sign In Button
    private fun onClickSplashScreenBtn() {
        mDataBinding!!.splashBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("Button Clicked", "true")
            Analytics.logEvent(bundle, "SplashScreen", "onClick")
//            firebaseAnalytics.logEvent("SplashScreen") {
//                param("OnClick", bundle)
//            }
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