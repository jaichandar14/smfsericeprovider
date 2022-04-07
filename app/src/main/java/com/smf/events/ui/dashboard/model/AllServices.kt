package com.smf.events.ui.dashboard.model


data class AllServices(
    val data: List<Data>,
    val result: Result,
    val success: Boolean
)


data class Data(
    val customerServiceCategory: Any,
    val questionnaireWrapperDto: Any,
    val serviceCategoryId: Int,
    val serviceCategoryTemplateDto: Any,
    val serviceName: String
)