package com.smf.events.ui.splash

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class SplashModule {
    @Provides
    fun provideViewModelProvider(viewModel: SplashScreenViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}