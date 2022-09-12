package com.smf.events.ui.signup

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import com.smf.events.network.ApiStories
import dagger.Module
import dagger.Provides

@Module
class SignUpModule {
    @Provides
    fun provideViewModelProvider(viewModel: SignUpViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

    @Provides
    fun provideSignUpRepository(apiStories: ApiStories): SignUpRepository {
        return SignUpRepository(apiStories)
    }
}