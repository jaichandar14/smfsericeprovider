package com.smf.events.ui.vieworderdetails.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.smf.events.ui.actionandstatusdashboard.model.Result
import kotlinx.parcelize.Parcelize

// 2402 - View order details  Model Data class
data class OrderDetails(
    val success: Boolean,
    @SerializedName("data")
    val data: DataValue,
    val result: Result,
)

@Parcelize
data class DataValue(
    val venueInformationDto: VenueInformationDto,
    var eventServiceQuestionnaireDescriptionDto: EventServiceQuestionnaireDescriptionDto?,
) : Parcelable

@Parcelize
data class VenueInformationDto(val zipCode: Int) : Parcelable

@Parcelize
data class EventServiceQuestionnaireDescriptionDto(
    var eventServiceDescriptionDto: EventServiceDescriptionDto?,
    var questionnaireWrapperDto: QuestionnaireWrapperDto?,
) : Parcelable

@Parcelize
data class EventServiceDescriptionDto(
    var eventServiceDateDto: EventServiceDateDto?,
    var eventServiceBudgetDto: EventServiceBudgetDto?,
    var eventServiceVenueDto: EventServiceVenueDto?,
) : Parcelable

@Parcelize
data class EventServiceDateDto(
    val biddingCutOffDate: String?,
    var serviceDate: String?,
    var preferredSlots: ArrayList<String>?,
) : Parcelable

@Parcelize
data class EventServiceBudgetDto(val currencyType: String, val estimatedBudget: Int) : Parcelable

@Parcelize
data class EventServiceVenueDto(val redius: String) : Parcelable

@Parcelize
data class QuestionnaireWrapperDto(
    val noOfVendors: String,
    val questionnaireDtos: List<QuestionnaireDtos>?,
) : Parcelable

@Parcelize
data class QuestionnaireDtos(val id: Int, val questionMetadata: QuestionMetadata?) : Parcelable

@Parcelize
data class QuestionMetadata(val question: String, val answer: String) : Parcelable