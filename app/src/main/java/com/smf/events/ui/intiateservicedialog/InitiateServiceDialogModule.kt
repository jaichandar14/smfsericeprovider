package com.smf.events.ui.intiateservicedialog

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import com.smf.events.ui.commoninformationdialog.CommonInfoDialogViewModel
import dagger.Module
import dagger.Provides
@Module
class InitiateServiceDialogModule  {
    @Provides
    fun provideViewModelProvider(viewModel: InitiateServiceDialogViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}