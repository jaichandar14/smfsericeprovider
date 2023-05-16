package com.smf.events.helper

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

//2401
@Singleton
class SharedPreference @Inject constructor(var application: Application) {

    companion object {
        const val ID_Token = "IdToken"
        const val BID_REQUEST_ID = "bidRequestId"
        const val EVENT_ID = "eventId"
        const val EVENT_DESCRIPTION_ID = "eventServiceDescriptionId"
        const val BID_REQUEST_ID_UPDATED = "bidRequestIdUpdated"
        const val ROLE_ID = "roleId"
        const val SP_REG_ID = "spRegId"
        const val FIRST_NAME = "userName"
        const val EMAIL_ID = "emailId"
        const val USER_ID = "userId"
        var isDialogShown: Boolean = false
    }

    private var sharedPreference: SharedPreferences =
        application.applicationContext.getSharedPreferences("MyUser", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor? = sharedPreference.edit()

    // PutString Method
    fun putString(key: String, value: String?) {
        editor?.putString(key, value)
        editor?.apply()
    }

    // Clear Method
    fun clear() {
        editor?.clear()
        editor?.apply()
    }

    // GetString Method
    fun getString(key: String): String? {
        return sharedPreference.getString(key, "")
    }

    // PutBoolean Method
    fun putBoolean(key: String, value: Boolean) {
        editor?.putBoolean(key, value)
        editor?.apply()
    }

    // GetBoolean Method
    fun getBoolean(key: String?): Boolean {
        return sharedPreference.getBoolean(key, false)
    }

    // PutInt Method
    fun putInt(key: String, value: Int) {
        editor?.putInt(key, value)
        editor?.apply()
    }

    // GetInt Method
    fun getInt(key: String?): Int {
        return sharedPreference.getInt(key, 0)
    }
}
