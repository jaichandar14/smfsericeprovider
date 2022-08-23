package com.smf.events.ui.notification

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class NotificationModule {

    @Provides
    fun provideViewModelProvider(viewModel: NotificationViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}