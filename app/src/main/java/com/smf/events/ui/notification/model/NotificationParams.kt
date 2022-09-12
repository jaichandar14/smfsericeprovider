package com.smf.events.ui.notification.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationParams(
    var fromNotification: Boolean = false,
    var backArrow: Boolean = false,
    var bidStatus: String? = "",
) : Parcelable
