package com.smf.events.ui.timeslotsexpandablelist

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class TimeSlotsExpandableListModule {
    @Provides
    fun provideViewModelProvider(viewModelExpandable: TimeSlotsExpandableListViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModelExpandable)
    }
}