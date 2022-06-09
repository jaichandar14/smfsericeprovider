package com.smf.events.ui.timeslot.deselectingdialog

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

// 2803 Module class
@Module
class DeselectingDialogModule {
    @Provides
    fun provideViewModelProvider(viewModel: DeselectingDialogViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}