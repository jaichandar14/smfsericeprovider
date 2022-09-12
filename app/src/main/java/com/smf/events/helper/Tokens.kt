package com.smf.events.helper

import android.util.Log
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import com.smf.events.SMFApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

class Tokens @Inject constructor() {

    // Method for verify token validity
    fun checkTokenExpiry(application: SMFApp, caller: String, idToken: String) {
        val newTime = Date().time / 1000
        val splitToken = idToken.split('.')
        Log.d(
            "TAG",
            "checkTokenExpiry refereshTokentime inside if block $splitToken ${splitToken.size}"
        )
        try {
            val decodedBytes =
                android.util.Base64.decode(splitToken[1], android.util.Base64.DEFAULT)
            val decodeToken = String(decodedBytes)
            val tokenObj = JSONObject(decodeToken)
            val tokenObjExp = tokenObj.getString("exp").toLong()
            val newTimeMin = newTime + 1 * 60
            if (newTimeMin < tokenObjExp) {
                Log.d("TAG", "checkTokenExpiry refereshTokentime inside if block")
                tokenNotExpired(idToken, myLambFunc, caller)
            } else {
                Log.d("TAG", "checkTokenExpiry refereshTokentime else block")
                fetchNewIdToken(application, myLambFunc, caller)
            }
        } catch (e: Exception) {
            Log.d("TAG", "checkTokenExpiry refereshTokentime exception block")
        }
    }

    //Method for fetching token
    private fun fetchNewIdToken(
        application: SMFApp,
        myFunc: suspend (String, String) -> Unit,
        caller: String,
    ) {
        Amplify.Auth.fetchAuthSession(
            {
                val session = it as AWSCognitoAuthSession
                val idToken =
                    AuthSessionResult.success(session.userPoolTokens.value?.idToken).value
                updateTokenToShardPreferences(application, idToken, myFunc, caller)
            },
            { Log.e("AuthQuickStart", "Failed to fetch session", it) })
    }

    // Method For Update New IdToken to Shared Preference
    private fun updateTokenToShardPreferences(
        application: SMFApp,
        idToken: String?,
        myFunc: suspend (String, String) -> Unit,
        caller: String,
    ) {
        SharedPreference(application).putString(SharedPreference.ID_Token, idToken)
        GlobalScope.launch {
            myFunc(
                "${AppConstants.BEARER} ${
                    SharedPreference(application).getString(SharedPreference.ID_Token)
                }",
                caller
            )
        }
    }

    //Method for Sending Not Expired Token
    private fun tokenNotExpired(
        idToken: String,
        myFunc: suspend (String, String) -> Unit,
        caller: String,
    ) {
        GlobalScope.launch {
            myFunc(idToken, caller)
        }
    }

    //Method for SignOut Current User
    private fun signOutCurrentUser(
        application: SMFApp,
        myFunc: suspend (String, String) -> Unit,
    ) {
        Amplify.Auth.signOut(
            {
                Log.i(
                    "AuthQuickstart",
                    "checkTokenExpiry refereshTokentime Signed out successfully"
                )
                SharedPreference(application).putString(SharedPreference.ID_Token, "")
                GlobalScope.launch {
                    myFunc("", "signOut")
                }
            },
            { Log.e("AuthQuickstart", "Sign out failed", it) }
        )
    }

    // Lambda Function for callBack
    private val myLambFunc: suspend (String, String) -> Unit = { token, caller ->
        idTokenCallBackInterface!!.tokenCallBack(token, caller)
    }

    private var idTokenCallBackInterface: IdTokenCallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: IdTokenCallBackInterface) {
        idTokenCallBackInterface = callback
    }

    // CallBack Interface
    interface IdTokenCallBackInterface {
        suspend fun tokenCallBack(idToken: String, caller: String)
    }

}