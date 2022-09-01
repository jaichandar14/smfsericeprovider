package com.smf.events.ui.emailotp

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EmailOTPFragmentTest{

    lateinit var emailOTPFragment: EmailOTPFragment

    @Before
    fun setUp() {
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