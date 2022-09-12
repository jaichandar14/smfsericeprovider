package com.smf.events.ui.notification

import android.app.Application
import com.smf.events.base.BaseViewModel
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    application: Application,
) : BaseViewModel(application)