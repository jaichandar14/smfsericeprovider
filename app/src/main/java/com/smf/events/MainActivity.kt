package com.smf.events


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.smf.events.base.BaseActivity
import com.smf.events.databinding.ActivityMainBinding
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun getContentView(): Int = R.layout.activity_main
    override fun getViewModel(): MainViewModel? =
        ViewModelProvider(this).get(MainViewModel::class.java)


    override fun getBindingVariable(): Int = BR.mainViewModel


    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        super.onPostCreate(null, null)
        // showToastMessage(resources.getString(R.string.Please_Enter_Any_EMail_or_Phone_Number),"000","jai")
        // 3104 FireBase Notification getInstance token method
        notificationFCMToken()
        // 3104 Firebase get payload data om background Method
        getPayloadData()
    }

    // 3104 FireBase Notification getInstance token method
    private fun getPayloadData() {
        if (intent.extras != null) {
            intent.extras!!.keySet().forEach {
                Log.d("TAG", "onCreate: ${it}")
                if (it.equals("key_1")) {
                    Log.d("TAG", "data details: ${intent.extras!![it]}")
                }
            }
        }
    }

    // 3104 Firebase get payload data om background Method
    private fun notificationFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            Log.d("TAG", "token data $token")
        })
    }

}

