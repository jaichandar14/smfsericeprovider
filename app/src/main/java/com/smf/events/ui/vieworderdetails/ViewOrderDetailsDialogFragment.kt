package com.smf.events.ui.vieworderdetails

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import androidx.lifecycle.ViewModelProvider
import com.smf.events.BR
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.FragmentViewOrderDetailsDialogBinding
import com.smf.events.helper.*
import com.smf.events.ui.vieworderdetails.adaptor.ViewOrderDetailsAdaptor
import com.smf.events.ui.vieworderdetails.model.EventServiceBudgetDto
import com.smf.events.ui.vieworderdetails.model.EventServiceDateDto
import com.smf.events.ui.vieworderdetails.model.QuestionnaireDtos
import com.smf.events.ui.vieworderdetails.model.VenueInformationDto
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ViewOrderDetailsDialogFragment(
    var eventId: Int,
    var eventServiceDescriptionId: Int,
    var eventDate: String,
    var eventName: String,
) : BaseDialogFragment<FragmentViewOrderDetailsDialogBinding, ViewOrderDetailsViewModel>(),
    Tokens.IdTokenCallBackInterface {

    companion object {
        const val TAG = "CustomDialogFragment"
        fun newInstance(
            eventId: Int,
            eventServiceDescriptionId: Int,
            eventDate: String,
            eventName: String,
        ): ViewOrderDetailsDialogFragment {
            return ViewOrderDetailsDialogFragment(eventId,
                eventServiceDescriptionId,
                eventDate,
                eventName)
        }
    }

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var tokens: Tokens

    lateinit var idToken: String
    lateinit var listView: ListView
    var preferedSlot = ArrayList<String>()
    var questionList = ArrayList<String>()
    var answerList = ArrayList<String>()
    private var myList = ArrayList<QuestionnaireDtos>()

    override fun getViewModel(): ViewOrderDetailsViewModel =
        ViewModelProvider(this, factory).get(ViewOrderDetailsViewModel::class.java)

    override fun getBindingVariable(): Int = BR.viewOrderDetailsViewModel

    override fun getContentView(): Int = R.layout.fragment_view_order_details_dialog

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
        // Initialize Local Variables
        setIdToken()
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.INVISIBLE
        listView = mDataBinding?.quesList!!
        // 2402 - token CallBackInterface
        tokens.setCallBackInterface(this)
        // 2402 - Api Token  validation ViewOrderDetails
        apiTokenValidationViewOrderDetails()
        // 2402 - Back Button pressed
        backButton()
    }

    // 2402 - Back Button pressed
    private fun backButton() {
        mDataBinding?.btnBack?.setOnClickListener {
            dismiss()
        }
    }

    // 2402 - View order details Get Api call
    private fun viewOrderDetails(idToken: String) {
        getViewModel().getViewOrderDetails(idToken, eventId, eventServiceDescriptionId)
            .observe(viewLifecycleOwner, { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        val venueInfo = apiResponse.response.data.venueInformationDto
                        val serviceDetails =
                            apiResponse.response.data.eventServiceQuestionnaireDescriptionDto?.eventServiceDescriptionDto?.eventServiceDateDto
                        val serviceBudget =
                            apiResponse.response.data.eventServiceQuestionnaireDescriptionDto?.eventServiceDescriptionDto?.eventServiceBudgetDto
                        val radius =
                            apiResponse.response.data.eventServiceQuestionnaireDescriptionDto?.eventServiceDescriptionDto?.eventServiceVenueDto?.redius
                        val questionnaire =
                            apiResponse.response.data.eventServiceQuestionnaireDescriptionDto?.questionnaireWrapperDto?.questionnaireDtos
                        settingOrderDetails(questionnaire,
                            venueInfo,
                            serviceDetails,
                            serviceBudget,
                            radius)
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
    }

    // 2402 - Setting the orderDetails Method
    @SuppressLint("SetTextI18n")
    private fun settingOrderDetails(
        questionnaireDtos: List<QuestionnaireDtos>?,
        venueInfo: VenueInformationDto,
        serviceDetails: EventServiceDateDto?,
        serviceBudget: EventServiceBudgetDto?,
        radius: String?,
    ) {
        mDataBinding?.question?.text = "Questions"
        mDataBinding?.txJobTitle?.text = eventName
        mDataBinding?.txJobIdnum?.text = eventServiceDescriptionId.toString()
        mDataBinding?.etEventDate?.text = DateFormatter.getDateFormat(eventDate)
        mDataBinding?.etZipCode?.text = venueInfo.zipCode.toString()
        mDataBinding?.etServiceDate?.text =
            DateFormatter.getDateFormat(serviceDetails?.serviceDate.toString())
        mDataBinding?.etBidCutOffDate?.text =
            DateFormatter.getDateFormat(serviceDetails?.biddingCutOffDate.toString())
        mDataBinding?.etEstimationBudget?.text =
            serviceBudget?.currencyType + " ${serviceBudget?.estimatedBudget}"
        mDataBinding?.etServiceRadius?.text = radius
        myList = questionnaireDtos as ArrayList
        for (i in myList.indices) {
            questionList.add(myList[i].questionMetadata?.question.toString())
            answerList.add(myList[i].questionMetadata?.answer.toString())
        }
        val slots = serviceDetails?.preferredSlots as ArrayList
        slots.forEach {
            preferedSlot.add(it)
        }
        val timing = preferedSlot.toString()
        mDataBinding?.etPreferedTimeSlot?.text = timing.substring(1, timing.length - 1)
        if (questionnaireDtos.isNullOrEmpty()) {
            mDataBinding?.question?.visibility = View.GONE
        } else {
            mDataBinding?.question?.visibility = View.VISIBLE
        }
        val myListAdapter = ViewOrderDetailsAdaptor(requireActivity(), questionList, answerList)
        listView.adapter = myListAdapter
        ListHelper.getListViewSize(listView)
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.VISIBLE
    }

    // 2402 - Setting IDToken
    private fun setIdToken() {
        idToken = "Bearer ${sharedPreference.getSharedPreferences().getString("IdToken", "")}"
    }

    // 2402 - Api Token Validation For Quote Brief Api Call
    private fun apiTokenValidationViewOrderDetails() {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp,
            "view_Order_Details", idToken
        )
    }

    // Call Back From Token Class
    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Dispatchers.Main) {
            // 2402 - View order details Get Api call
            viewOrderDetails(idToken)
        }
    }
}