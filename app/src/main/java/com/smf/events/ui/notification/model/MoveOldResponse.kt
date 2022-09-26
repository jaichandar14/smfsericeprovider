package com.smf.events.ui.notification.model

data class MoveOldResponse(
    val `data`: Datas
)

data class Datas(
    val key: Int,
    val profileId: Int,
    val status: String,
    val statusCode: Int
)