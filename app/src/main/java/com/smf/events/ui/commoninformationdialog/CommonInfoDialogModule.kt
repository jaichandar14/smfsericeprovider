package com.smf.events.ui.commoninformationdialog

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import com.smf.events.ui.quotedetailsdialog.QuoteDetailsDialogViewModel
import dagger.Module
import dagger.Provides

@Module
class CommonInfoDialogModule {
    @Provides
    fun provideViewModelProvider(viewModel: CommonInfoDialogViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}