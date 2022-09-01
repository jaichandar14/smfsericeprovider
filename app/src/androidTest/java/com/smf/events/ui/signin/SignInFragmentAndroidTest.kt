package com.smf.events.ui.signin

import android.content.Context
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInFragmentAndroidTest {

    private lateinit var appContext: Context
    lateinit var signInFragment: SignInFragment

    @Before
    fun setUp() {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getInstrumentation().context
        signInFragment = SignInFragment()
    }

    @Test
    fun checkNumberValidation() {
        Assert.assertEquals(
            signInFragment.emailAndNumberValidation(
                "8667636458",
                ""
            ), true
        )
    }

    @Test
    fun checkEmailValidation() {
        Assert.assertEquals(
            signInFragment.emailAndNumberValidation(
                "",
                "vigneshwaran.p996@gmail.com"
            ), true
        )
    }

    @Test
    fun checkNumberAndEmailValidation() {
        Assert.assertEquals(
            signInFragment.emailAndNumberValidation(
                "8667636458",
                "vigneshwaran.p996@gmail.com"
            ), false
        )
    }

}