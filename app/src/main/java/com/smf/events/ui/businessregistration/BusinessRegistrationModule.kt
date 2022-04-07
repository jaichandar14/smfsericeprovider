package com.smf.events.ui.businessregistration

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class BusinessRegistrationModule {

    @Provides
    fun provideViewModelProvider(viewModel: BusinessRegistrationViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}