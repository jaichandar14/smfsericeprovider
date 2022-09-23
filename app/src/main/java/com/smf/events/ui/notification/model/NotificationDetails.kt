package com.smf.events.ui.notification.model

data class NotificationDetails(
    val notificationId: Int,
    val notificationDate: String,
    val notificationType: String,
    val notificationTitle: String,
    val notificationContent: String
)