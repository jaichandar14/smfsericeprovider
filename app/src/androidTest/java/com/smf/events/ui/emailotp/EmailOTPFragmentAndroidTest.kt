package com.smf.events.ui.emailotp

import android.content.Context
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmailOTPFragmentAndroidTest {

    private lateinit var appContext: Context
    private lateinit var emailOTPFragment: EmailOTPFragment

    @Before
    fun setUp() {
        // Context of the app under test.
        appContext = InstrumentationRegistry.getInstrumentation().context
        emailOTPFragment = EmailOTPFragment()
    }

    @Test
    fun validOtp() {
        assertEquals(
            emailOTPFragment.otpValidation("1", "2", "3", "4"), true
        )
    }

    @Test
    fun inValidOtp() {
        assertEquals(
            emailOTPFragment.otpValidation("", "", "", ""), false
        )
    }

}