package com.smf.events.ui.actionandstatusdashboard

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class ActionsAndStatusModule {
    @Provides
    fun provideViewModelProvider(viewModel: ActionsAndStatusViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}