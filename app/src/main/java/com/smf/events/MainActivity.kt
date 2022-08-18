package com.smf.events


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.smf.events.base.BaseActivity
import com.smf.events.databinding.ActivityMainBinding
import com.smf.events.helper.SnackBar
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector

import javax.inject.Inject

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    override fun getContentView(): Int = R.layout.activity_main
    override fun getViewModel(): MainViewModel? =
        ViewModelProvider(this).get(MainViewModel::class.java)


    override fun getBindingVariable(): Int = BR.mainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        super.onPostCreate(null, null)
       // showToastMessage(resources.getString(R.string.Please_Enter_Any_EMail_or_Phone_Number),"000","jai")
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
    }
}


