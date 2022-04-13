package com.smf.events.ui.vieworderdetails

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

//2402
@Module
class ViewOrderDetailsModule {
    @Provides
    fun provideViewModelProvider(viewModel: ViewOrderDetailsViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}