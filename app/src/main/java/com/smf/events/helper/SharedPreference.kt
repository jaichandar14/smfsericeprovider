package com.smf.events.helper

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

//2401
@Singleton
class SharedPreference @Inject constructor(var application: Application) {

    // 2401 - Method For Get Singleton SharedPreferences Object
    fun getSharedPreferences(): SharedPreferences {
        return application.applicationContext.getSharedPreferences("MyUser", Context.MODE_PRIVATE)
    }

}