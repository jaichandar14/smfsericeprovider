package com.smf.events.ui.signup

import android.app.Application
import android.util.Log
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import com.smf.events.base.BaseViewModel
import com.smf.events.databinding.FragmentSignupBinding
import com.smf.events.ui.signup.model.UserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.roundToInt

class SignUpViewModel @Inject constructor(
    private val signUpRepository: SignUpRepository,
    application: Application
) : BaseViewModel(application) {

    private lateinit var userName: String
    private lateinit var password: String
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var email: String
    private lateinit var mobileNumber: String
    private lateinit var phoneNumber: String
    lateinit var userDetails: UserDetails

    // Method for SignUp Function
    fun signUpFunctionality(mDataBinding: FragmentSignupBinding, role: String) {
        firstName = mDataBinding.editTextFirstName.text.toString().trim()
        lastName = mDataBinding.editTextLastName.text.toString().trim()
        email = mDataBinding.editTextEmail.text.toString().trim()
        var countryCode = mDataBinding.cpp.selectedCountryCode
        phoneNumber = mDataBinding.editTextNumber.text?.trim().toString()
        mobileNumber = "+".plus(countryCode).plus(phoneNumber)

        val attrs = mapOf(
            AuthUserAttributeKey.email() to email,
            AuthUserAttributeKey.phoneNumber() to "+".plus(countryCode).plus(phoneNumber)
        )

        val options = AuthSignUpOptions.builder()
            .userAttribute(
                AuthUserAttributeKey.name(),
                firstName.plus(" ").plus(lastName)
            )
            .userAttributes(attrs.map { AuthUserAttribute(it.key, it.value) })
            .build()

        userName = createUserName(firstName, lastName)
        password = "Service@123"
        Log.d("TAG", "signUpFunctionality: $role")
        userDetails = UserDetails(role, firstName, lastName, email, mobileNumber, userName)

        //sign up conditions
        signUpInputvalidation(userName, password, mDataBinding, options)
    }

    //sign up conditions
    fun signUpInputvalidation(
        userName: String,
        password: String,
        mDataBinding: FragmentSignupBinding?,
        options: AuthSignUpOptions
    ) {
        firstName = mDataBinding?.editTextFirstName?.text.toString().trim()
        lastName = mDataBinding?.editTextLastName?.text.toString().trim()
        email = mDataBinding?.editTextEmail?.text.toString().trim()
        phoneNumber = mDataBinding?.editTextNumber?.text?.trim().toString()

        if ((firstName.isEmpty() && lastName.isEmpty() && email.isEmpty()) && phoneNumber.isEmpty()) {
            toastMessage = ("All fields are mandatory")
            callBackInterface!!.authResponse()
        } else {
            signUp(userName, password, options)
        }
    }

    // SignUp Function
    fun signUp(userName: String, password: String, options: AuthSignUpOptions) {

        Amplify.Auth.signUp(
            userName, password, options,
            {
                Log.i("AuthQuickstart", "Sign up result = $it")
                viewModelScope.launch {
                    callBackInterface?.callBack("signUpResult", "Success", userDetails, userName)
                }

            },
            {
                Log.i("AuthQuickstart", "Sign up failed 1${it.cause!!.message!!.split(".")[0]}")
                viewModelScope.launch {
                    var errmesg = it.cause!!.message!!.split(".")[0]
                    callBackInterface?.callBack("signUpFailed", errmesg, userDetails, userName)
                }
            }
        )
    }

    // Method Creating Unique UserName
    private fun createUserName(firstName: String, lastName: String): String {
        var first4DigitName = ""
        var last4DigitName = ""
        var randomValue = floor(Math.random() * (999999 - 100000)) + 100000

        first4DigitName = if (firstName.length > 4) {
            firstName.substring(0, 4)
        } else {
            firstName
        }
        last4DigitName = if (lastName.length > 4) {
            lastName.substring(0, 4)
        } else {
            lastName
        }
        return last4DigitName.plus(randomValue.roundToInt()).plus(first4DigitName)
    }

    // passing the userdetails in io to repository
    fun setUserDetails(userDetails: UserDetails) = liveData(Dispatchers.IO) {
        try {
            emit(signUpRepository.setUserDetails(userDetails))
        } catch (e: Exception) {
            Log.d("TAG", "setUserDetails: $e")
        }
    }

    private var callBackInterface: CallBackInterface? = null

    // Initializing CallBack Interface Method
    fun setCallBackInterface(callback: CallBackInterface) {
        callBackInterface = callback
    }

    // CallBackInterface
    interface CallBackInterface {
        fun callBack(status: String, message: String?, userDetails: UserDetails, userName: String)
        fun authResponse()
    }
}