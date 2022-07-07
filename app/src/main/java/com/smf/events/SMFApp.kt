package com.smf.events

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.example.demodragger.di.DaggerAppComponent
import com.smf.events.helper.Tokens
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class SMFApp:Application(), HasAndroidInjector {

    private var mContext: SMFApp? = null

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        mContext = this
        //Dagger Initialization
        DaggerAppComponent.builder()
            .application(this)
            .build().inject(this)

        //Amplify Cognito Integration
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            var resourceId = if(BuildConfig.FLAVOR=="dev")R.raw.amplifyconfiguration
            else if(BuildConfig.FLAVOR=="qa") R.raw.qa_aws
            else R.raw.amplifyconfigurationuat
            val config=AmplifyConfiguration.fromConfigFile(applicationContext,resourceId)
            Amplify.configure(config,applicationContext)
            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }

    }

    fun getContext(): SMFApp? {
        return mContext
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }


}