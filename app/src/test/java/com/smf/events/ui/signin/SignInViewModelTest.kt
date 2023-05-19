package com.smf.events.ui.signin

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.smf.events.network.ApiStories
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
class SignInViewModelTest {

    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var signInRepository: SignInRepository

    @Mock
    lateinit var apiStories: ApiStories

    @Mock
    private lateinit var context: Application
    private lateinit var viewModel: SignInViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        signInRepository = SignInRepository(apiStories)
        viewModel = Mockito.mock(SignInViewModel(signInRepository, context)::class.java)
    }

    @Test
    fun signIn() {
        viewModel.signIn("vignesh", context)
        Mockito.verify(viewModel, Mockito.times(1)).signIn("vignesh", context)
    }

    @Test
    fun resendSignUp() {
        viewModel.resendSignUp("vignesh", context)
        Mockito.verify(viewModel, Mockito.times(1)).resendSignUp("vignesh", context)
    }

    @Test
    fun getUserDetails() {
        viewModel.getUserDetails("vignesh")
        Mockito.verify(viewModel, Mockito.times(1)).getUserDetails("vignesh")
    }

}