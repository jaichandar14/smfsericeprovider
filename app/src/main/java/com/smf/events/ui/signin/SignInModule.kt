package com.smf.events.ui.signin

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class SignInModule {
    @Provides
    fun provideViewModelProvider(viewModel: SignInViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}