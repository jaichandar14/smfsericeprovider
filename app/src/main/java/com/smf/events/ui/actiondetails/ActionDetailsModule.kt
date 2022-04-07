package com.smf.events.ui.actiondetails

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class ActionDetailsModule {
    @Provides
    fun provideViewModelProvider(viewModel: ActionDetailsViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}