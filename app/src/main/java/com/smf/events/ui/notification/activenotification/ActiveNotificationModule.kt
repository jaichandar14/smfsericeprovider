package com.smf.events.ui.notification.activenotification

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class ActiveNotificationModule {

    @Provides
    fun provideViewModelProvider(viewModel: ActiveNotificationViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}