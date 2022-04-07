package com.smf.events.ui.emailotp

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import com.smf.events.ui.signin.SignInViewModel
import dagger.Module
import dagger.Provides

@Module
class EmailOTPModule {

    @Provides
    fun provideViewModelProvider(viewModel: EmailOTPViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}