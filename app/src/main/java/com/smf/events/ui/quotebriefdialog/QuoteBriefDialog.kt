package com.smf.events.ui.quotebriefdialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.QuoteBriefDialogBinding
import com.smf.events.helper.*
import com.smf.events.ui.quotebrief.model.QuoteBrief
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class QuoteBriefDialog(var status: Int) :
    BaseDialogFragment<QuoteBriefDialogBinding, QuoteBriefDialogViewModel>(),
    Tokens.IdTokenCallBackInterface {

    var bidRequestId: Int? = 0
    lateinit var idToken: String
    var expand = false
    lateinit var bidStatus: String

    companion object {
        const val TAG = "CustomDialogFragment"
        fun newInstance(status: Int): QuoteBriefDialog {
            return QuoteBriefDialog(status)
        }
    }

    init {
        bidRequestId = status
    }

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreference

    @Inject
    lateinit var tokens: Tokens

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
        Log.d(TAG, "onCreate: new data and design")
        setIdTokenAndBidReqId()
    }

    override fun onStart() {
        super.onStart()
        apiTokenValidationQuoteBrief()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.INVISIBLE
        mDataBinding?.progressBar?.visibility=View.VISIBLE

        // token CallBackInterface
        tokens.setCallBackInterface(this)
        // Back Button Pressed
        mDataBinding?.btnBack?.setOnClickListener {
            backButtonClickListener()
        }
        // Expandable view
        getViewModel().expandableView(mDataBinding, expand)
    }

    // Back Button Pressed
    private fun backButtonClickListener() {
        parentFragmentManager.setFragmentResult(
            "1", // Same request key DashBoardFragment used to register its listener
            bundleOf("key" to "value") // The data to be passed to DashBoardFragment
        )
        dismiss()
    }

    // Setting Bid Submitted Quote
    private fun setBidSubmitQuoteBrief(response: QuoteBrief) {
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.VISIBLE
        mDataBinding?.progressBar?.visibility=View.INVISIBLE
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txCatering?.text = "${response.data.serviceName}-${response.data.branchName}"
        // 2835
        mDataBinding?.serviceName?.text = response.data.serviceName
        mDataBinding?.branchName?.text = response.data.branchName
        mDataBinding?.txJobTitle?.text = response.data.eventName
        // 2354
        val currencyType = setCurrencyType(response)
        if (response.data.costingType == "Bidding") {
            mDataBinding?.txJobAmount?.text = "$currencyType${response.data.latestBidValue}"
        } else {
            mDataBinding?.txJobAmount?.text = "$currencyType${response.data.cost}"
        }
        mDataBinding?.txJobIdnum?.text = response.data.eventServiceDescriptionId.toString()
        mDataBinding?.txEventdateValue?.text = DateFormatter.getDateFormat(response.data.eventDate)
        mDataBinding?.txBidProposalDateValue?.text =
            DateFormatter.getDateFormat(response.data.bidRequestedDate)
        mDataBinding?.txCutOffDateValue?.text =
            DateFormatter.getDateFormat(response.data.biddingCutOffDate)
        mDataBinding?.serviceDateValue?.text =
            DateFormatter.getDateFormat(response.data.serviceDate)
        mDataBinding?.paymentStatusValue?.text = "NA"
        mDataBinding?.servicedBy?.text = "NA"
        mDataBinding?.address?.text = "${response.data.serviceAddressDto.addressLine1}  " +
                "${response.data.serviceAddressDto.addressLine2}   " +
                "${response.data.serviceAddressDto.city}"
        mDataBinding?.customerRating?.text = "NA"

        mDataBinding?.progressBar?.visibility=View.INVISIBLE

    }

    // 2354 - Method For Setting CurrencyType
    private fun setCurrencyType(response: QuoteBrief): String {
        val currencyType = if (response.data.currencyType == null) {
            "$"
        } else {
            when (response.data.currencyType) {
                "USD($)" -> "$"
                "GBP(\u00a3)" -> "\u00a3"
                "INR(\u20B9)" -> "₹"
                else -> {
                    "$"
                }
            }
        }
        return currencyType
    }

    // Setting Bid Pending Quote
    @SuppressLint("SetTextI18n")
    private fun setPendingQuoteBrief(response: QuoteBrief) {
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.VISIBLE
        mDataBinding?.progressBar?.visibility=View.INVISIBLE
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txCatering?.text = "${response.data.serviceName}-${response.data.branchName}"
        mDataBinding?.serviceName?.text = response.data.serviceName
        mDataBinding?.branchName?.text = response.data.branchName
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txJobAmount?.visibility = View.INVISIBLE
        mDataBinding?.viewQuote?.visibility = View.INVISIBLE
        mDataBinding?.spnBidAccepted?.text = "Pending For Quote"
        mDataBinding?.check1?.visibility = View.INVISIBLE
        mDataBinding?.txJobIdnum?.text = response.data.eventServiceDescriptionId.toString()
        mDataBinding?.txEventdateValue?.text = DateFormatter.getDateFormat(response.data.eventDate)
        mDataBinding?.txBidProposalDateValue?.text =
            DateFormatter.getDateFormat(response.data.bidRequestedDate)
        mDataBinding?.txCutOffDateValue?.text =
            DateFormatter.getDateFormat(response.data.biddingCutOffDate)
        mDataBinding?.serviceDateValue?.text =
            DateFormatter.getDateFormat(response.data.serviceDate)
        mDataBinding?.paymentStatusValue?.text = "NA"
        mDataBinding?.servicedBy?.text = "NA"
        mDataBinding?.address?.text = "${response.data.serviceAddressDto.addressLine1}  " +
                "${response.data.serviceAddressDto.addressLine2}   " +
                "${response.data.serviceAddressDto.city}"
        mDataBinding?.customerRating?.text = "NA"
    }

    // Setting IDToken
    private fun setIdTokenAndBidReqId() {
        bidRequestId = sharedPreference.getInt(SharedPreference.BID_REQUEST_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
    }

    // 2904 Get Api Call for getting the Quote Brief
    private fun quoteBriefApiCall(idToken: String) {
        getViewModel().getQuoteBrief(idToken, status)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        bidStatus = apiResponse.response.data.bidStatus
                        when (bidStatus) {
                            "BID SUBMITTED" -> {
                                setBidSubmitQuoteBrief(apiResponse.response)
                                mDataBinding?.spnBidAccepted?.text = "Bidding in progress"
                            }
                            "PENDING FOR QUOTE" -> setPendingQuoteBrief(apiResponse.response)
                            // 2904 Won Bid Flow for Start Sevice
                            AppConstants.WON_BID -> {
                                widgetWonBid(apiResponse)
                            }
                            AppConstants.SERVICE_IN_PROGRESS -> {
                                widgetServiceProgress(apiResponse)
                            }
                            AppConstants.SERVICE_DONE -> {
                                widgetServiceCloser(apiResponse)
                            }
                        }
                    }
                    is ApisResponse.Error -> {
                        Log.d("TAG", "check token result: ${apiResponse.exception}")
                        mDataBinding?.progressBar?.visibility=View.INVISIBLE
                    }
                    else -> {
                    }
                }
            })
    }

    // 2904 Method for won bid service start flow
    fun widgetWonBid(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = "Bid won"
        mDataBinding?.txWonBid?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.inprogress)
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    // 2904 Method for Initiate closer flow
    fun widgetServiceProgress(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.green_check)
        mDataBinding?.check4?.setImageResource(R.drawable.inprogress)
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow3?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = "Service in progress"
        setBidSubmitQuoteBrief(apiResponse.response)
    }
    fun widgetServiceCloser(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.green_check)
        mDataBinding?.check4?.setImageResource(R.drawable.green_check)
        mDataBinding?.check5?.setImageResource(R.drawable.green_check)
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow5?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow3?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = "Service in progress"
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    // Call Back From Token Class
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