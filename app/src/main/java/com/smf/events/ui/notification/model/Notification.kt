package com.smf.events.ui.notification.model

data class Notification(
    val `data`: List<Data>,
    val result: Result,
    val success: Boolean
)

data class Data(
    val createdDate: String,
    val formatedCreatedDate: String,
    val isActive: Boolean,
    val moduleType: String,
    val notificationContent: String,
    val notificationId: Int,
    val notificationMetadata: NotificationMetadata,
    val notificationTitle: String,
    val notificationType: String,
    val userId: String,
    val viewedDate: Any
)

data class NotificationMetadata(
    val bidRequestId: Int,
    val eventId: Int,
    val eventServiceDescriptionId: Int,
    val serviceVendorOnboardingId: Int
)

data class Result(
    val info: String
)

