package com.smf.events.ui.schedulemanagement

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

// 2458
@Module
class ScheduleManagementModule {
    @Provides
    fun provideViewModelProvider(viewModel: ScheduleManagementViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}