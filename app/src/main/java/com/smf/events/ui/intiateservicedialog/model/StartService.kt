package com.smf.events.ui.commoninformationdialog.model

data class StartServiceDialog(
    val success: Boolean,
    val datas: List<Datas>,
    val result: Results,
)


data class Datas(
    val docId: String,
    val key: Int,
    val status: String,
    val statusCode: Int,
)

data class Results(val info: String, val systemMessage: String)
