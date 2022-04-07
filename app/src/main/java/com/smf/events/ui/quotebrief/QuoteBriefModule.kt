package com.smf.events.ui.quotebrief

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory

import dagger.Module
import dagger.Provides

@Module
class QuoteBriefModule {
    @Provides
    fun provideViewModelProvider(viewModel: QuoteBriefViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}