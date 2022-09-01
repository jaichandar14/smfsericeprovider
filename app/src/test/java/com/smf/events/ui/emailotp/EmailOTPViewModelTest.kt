package com.smf.events.ui.emailotp

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.smf.events.network.ApiStories
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class EmailOTPViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var emailOTPRepository: EmailOTPRepository

    @Mock
    lateinit var apiStories: ApiStories

    @Mock
    private lateinit var context: Application
    private lateinit var viewModel: EmailOTPViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        emailOTPRepository = EmailOTPRepository(apiStories)
//        viewModel = EmailOTPViewModel(emailOTPRepository, context)
        viewModel = mock(EmailOTPViewModel(emailOTPRepository, context)::class.java)
    }

    @Test
    fun getLoginInfo() {
        viewModel.getLoginInfo("idToken")
        verify(viewModel, times(1)).getLoginInfo("idToken")
    }

    @Test
    fun getOtpValidation() {
        viewModel.getOtpValidation(true, "vignesh")
        verify(viewModel, times(1)).getOtpValidation(true, "vignesh")
    }

}