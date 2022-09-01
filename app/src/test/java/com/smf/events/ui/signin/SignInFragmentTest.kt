package com.smf.events.ui.signin

import android.app.Application
import com.smf.events.network.ApiStories
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(JUnit4::class)
class SignInFragmentTest{

    lateinit var signInFragment: SignInFragment

    @Before
    fun setUp() {
        signInFragment = SignInFragment()
    }

    @Test
    fun checkNumberValidation() {
        assertEquals(
            signInFragment.emailAndNumberValidation(
                "8667636458",
                ""
            ), true
        )
    }

    @Test
    fun checkEmailValidation() {
        assertEquals(
            signInFragment.emailAndNumberValidation(
                "",
                "vigneshwaran.p996@gmail.com"
            ), true
        )
    }

    @Test
    fun checkNumberAndEmailValidation() {
        assertEquals(
            signInFragment.emailAndNumberValidation(
                "8667636458",
                "vigneshwaran.p996@gmail.com"
            ), false
        )
    }
}