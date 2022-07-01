package com.smf.events.ui.commoninformationdialog.model

data class StartService(
    val success: Boolean,
    val datas: List<Data>,
    val result: Result,
)


data class Data(
    val docId: String,
    val key: Int,
    val status: String,
    val statusCode: Int,
)

data class Result(val info: String, val systemMessage: String)
