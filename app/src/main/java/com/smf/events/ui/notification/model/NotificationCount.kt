package com.smf.events.ui.notification.model

data class NotificationCount(
    val `data`: DataCount
)

data class DataCount(
    val activeCounts: Int,
    val oldCounts: Int
)