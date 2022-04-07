package com.smf.events.ui.dashboard

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class DashBoardModule {
    @Provides
    fun provideViewModelProvider(viewModel: DashBoardViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}