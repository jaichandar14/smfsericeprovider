package com.smf.events.ui.emailotp.model

data class GetLoginInfo(
    val data: Datas,
    val success: Boolean
)

data class Datas(
    val email: String,
    val firstName: String,
    val isActive: Boolean,
    val lastName: String,
    val mobileNumber: String,
    val role: String,
    val roleId: Int,
    val spRegId: Int,
    val userId: Int,
    val userName: String,
    val userStatus: String
)
