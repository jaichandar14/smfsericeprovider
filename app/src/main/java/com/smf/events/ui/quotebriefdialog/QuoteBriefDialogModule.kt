package com.smf.events.ui.quotebriefdialog

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class QuoteBriefDialogModule {
    @Provides
    fun provideViewModelProvider(viewModel: QuoteBriefDialogViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}