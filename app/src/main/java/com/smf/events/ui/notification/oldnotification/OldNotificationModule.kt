package com.smf.events.ui.notification.oldnotification

import androidx.lifecycle.ViewModelProvider
import com.smf.events.helper.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class OldNotificationModule {

    @Provides
    fun provideViewModelProvider(viewModel: OldNotificationViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}