package com.smf.events.ui.quotebriefdialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.QuoteBriefDialogBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.Tokens
import com.smf.events.ui.quotebrief.model.QuoteBrief
import com.smf.events.ui.quotedetailsdialog.QuoteDetailsDialog.Companion.currencyType
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Month
import javax.inject.Inject


class QuoteBriefDialog : BaseDialogFragment<QuoteBriefDialogBinding, QuoteBriefDialogViewModel>(),
    Tokens.IdTokenCallBackInterface {
    companion object {
        const val TAG = "CustomDialogFragment"

        fun newInstance(

        ): QuoteBriefDialog {

            return QuoteBriefDialog()
        }
    }

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var tokens: Tokens
    var bidRequestId: Int? = 0
    lateinit var idToken: String
    var expand = false
    lateinit var bidStatus: String

    override fun getViewModel(): QuoteBriefDialogViewModel =
        ViewModelProvider(this, factory).get(QuoteBriefDialogViewModel::class.java)

    override fun getBindingVariable(): Int = BR.quoteBriefDialogViewModel

    override fun getContentView(): Int = R.layout.quote_brief_dialog

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
        // Initialize Local Variables
        setIdTokenAndBidReqId()
    }

    override fun onStart() {
        super.onStart()
        apiTokenValidationQuoteBrief()

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.INVISIBLE
        // token CallBackInterface
        tokens.setCallBackInterface(this)
        //Back Button Pressed
        mDataBinding?.btnBack?.setOnClickListener {
            backButtonClickListener()
        }
        //Expandable view
        getViewModel().expandableView(mDataBinding, expand)

//        //state progress three completed
//        getViewModel()?.progress3Completed(mDataBinding)
//        //state progress four completed
//        getViewModel()?.progress4Completed(mDataBinding)
    }

    //Back Button Pressed
    private fun backButtonClickListener() {
        parentFragmentManager.setFragmentResult(
            "1", // Same request key DashBoardFragment used to register its listener
            bundleOf("key" to "value") // The data to be passed to DashBoardFragment
        )
        dismiss()
    }

    //Setting Bid Submitted Quote
    private fun setBidSubmitQuoteBrief(response: QuoteBrief) {
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.VISIBLE
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txCatering?.text = "${response.data.serviceName}-${response.data.branchName}"
        mDataBinding?.txJobTitle?.text = response.data.eventName

//        if (response.data.currencyType == null) {
//
//        } else {
//            var currencyType = when (response.data.currencyType) {
//                "USD($)" -> "$"
//                "GBP(\u00a3)" -> "\u00a3"
//                "INR(\u20B9)" -> "₹"
//                else -> {}
//            }
//        }

        var currencyType = "$"
        Log.d(TAG, "setBidSubmitQuoteBrief: $currencyType")


        if (response.data.costingType == "Bidding") {
            mDataBinding?.txJobAmount?.text = "$currencyType${response.data.latestBidValue}"
        } else {
            mDataBinding?.txJobAmount?.text = "$currencyType${response.data.cost}"
        }
        mDataBinding?.txJobIdnum?.text = response.data.eventServiceDescriptionId.toString()
        mDataBinding?.txEventdateValue?.text = dateFormat(response.data.eventDate)
        mDataBinding?.txBidProposalDateValue?.text = dateFormat(response.data.bidRequestedDate)
        mDataBinding?.txCutOffDateValue?.text = dateFormat(response.data.biddingCutOffDate)
        mDataBinding?.serviceDateValue?.text = dateFormat(response.data.serviceDate)
        mDataBinding?.paymentStatusValue?.text = "NA"
        mDataBinding?.servicedBy?.text = "NA"
        mDataBinding?.address?.text = "${response.data.serviceAddressDto.addressLine1}  " +
                "${response.data.serviceAddressDto.addressLine2}   " +
                "${response.data.serviceAddressDto.city}"
        mDataBinding?.customerRating?.text = "NA"


    }

    //Setting Bid Pending Quote
    private fun setPendingQuoteBrief(response: QuoteBrief) {
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.VISIBLE
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txCatering?.text = "${response.data.serviceName}-${response.data.branchName}"
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txJobAmount?.visibility = View.INVISIBLE
        mDataBinding?.viewQuote?.visibility = View.INVISIBLE
        mDataBinding?.spnBidAccepted?.text = "Pending For Quote"
        mDataBinding?.check1?.visibility = View.INVISIBLE
        mDataBinding?.check1inprogress?.visibility = View.VISIBLE
        mDataBinding?.txJobIdnum?.text = response.data.eventServiceDescriptionId.toString()
        mDataBinding?.txEventdateValue?.text = dateFormat(response.data.eventDate)
        mDataBinding?.txBidProposalDateValue?.text = dateFormat(response.data.bidRequestedDate)
        mDataBinding?.txCutOffDateValue?.text = dateFormat(response.data.biddingCutOffDate)
        mDataBinding?.serviceDateValue?.text = dateFormat(response.data.serviceDate)
        mDataBinding?.paymentStatusValue?.text = "NA"
        mDataBinding?.servicedBy?.text = "NA"
        mDataBinding?.address?.text = "${response.data.serviceAddressDto.addressLine1}  " +
                "${response.data.serviceAddressDto.addressLine2}   " +
                "${response.data.serviceAddressDto.city}"
        mDataBinding?.customerRating?.text = "NA"


    }

    //Date Formatted for setting details
    private fun dateFormat(input: String): String {
        var monthCount = input.substring(0, 2)
        val date = input.substring(3, 5)
        val year = input.substring(6, 10)
        if (monthCount[0].digitToInt() == 0) {
            monthCount = monthCount[1].toString()
        }
        val month = Month.of(monthCount.toInt()).toString().substring(0, 3)
        return "$date $month $year"
    }

    //Setting IDToken
    private fun setIdTokenAndBidReqId() {
        var getSharedPreferences = requireActivity().applicationContext.getSharedPreferences(
            "MyUser",
            Context.MODE_PRIVATE
        )
        bidRequestId = getSharedPreferences?.getInt("bidRequestId", 0)
        idToken = "Bearer ${getSharedPreferences?.getString("IdToken", "")}"
    }

    //Get Api Call for getting the Quote Brief
    private fun quoteBriefApiCall(idToken: String) {
        getViewModel().getQuoteBrief(idToken, bidRequestId!!)
            .observe(viewLifecycleOwner, Observer { apiResponse ->

                when (apiResponse) {
                    is ApisResponse.Success -> {

                        Log.d("TAG", "Quotedetails Succcess: ${(apiResponse.response)}")
                        apiResponse.response.data.eventDate

                        bidStatus = apiResponse.response.data.bidStatus

                        when (bidStatus) {

                            "BID SUBMITTED" -> setBidSubmitQuoteBrief(apiResponse.response)
                            "PENDING FOR QUOTE" -> setPendingQuoteBrief(apiResponse.response)
                        }

                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                    }
                    else -> {
                    }
                }
            })
    }


    //Call Back From Token Class
    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Dispatchers.Main) {
            quoteBriefApiCall(idToken)
        }
    }

    // Api Token Validation For Quote Brief Api Call
    private fun apiTokenValidationQuoteBrief() {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp,
            "quote_brief", idToken
        )
    }
}