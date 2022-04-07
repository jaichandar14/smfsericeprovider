package com.smf.events.ui.quotebrief


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseFragment
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.Tokens
import com.smf.events.ui.quotebrief.model.QuoteBrief
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.time.Month
import javax.inject.Inject


class QuoteBriefFragment :
    BaseFragment<com.smf.events.databinding.FragmentQuoteBriefBinding, QuoteBriefViewModel>(),
    QuoteBriefViewModel.CallBackInterface, Tokens.IdTokenCallBackInterface {
    var expand = false

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var tokens: Tokens
    var bidRequestId: Int? = 0
    lateinit var idToken: String

    lateinit var bidStatus: String
    override fun getViewModel(): QuoteBriefViewModel? =
        ViewModelProvider(this, factory).get(QuoteBriefViewModel::class.java)

    override fun getBindingVariable(): Int = BR.quoteBriefViewModel

    override fun getContentView(): Int = R.layout.fragment_quote_brief

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setIdTokenAndSpRegId()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // token CallBackInterface
        tokens.setCallBackInterface(this)

        getViewModel()?.backButtonPressed(mDataBinding!!)

        getViewModel()?.setCallBackInterface(this)


        getViewModel()?.expandableView(mDataBinding, expand)


        apiTokenValidationQuoteBrief()

//        //state progress three completed
//        getViewModel()?.progress3Completed(mDataBinding)
//        //state progress four completed
//        getViewModel()?.progress4Completed(mDataBinding)
    }

    override fun callBack(messages: String) {

        when (messages) {

            "onBackClicked" -> {
                findNavController().navigate(
                    QuoteBriefFragmentDirections.actionQuoteBriefFragmentToDashBoardFragment()
                )
            }
        }
    }


    fun setBidSubmitQuoteBrief(response: QuoteBrief) {
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txCatering?.text = "${response.data.serviceName}-${response.data.branchName}"
        mDataBinding?.txJobTitle?.text = response.data.eventName
        if (response.data.costingType == "Bidding") {
            mDataBinding?.txJobAmount?.text = "$${response.data.latestBidValue}"
        } else {
            mDataBinding?.txJobAmount?.text = "$${response.data.cost}"
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

    private fun setIdTokenAndSpRegId() {
        var getSharedPreferences = requireActivity().applicationContext.getSharedPreferences(
            "MyUser",
            Context.MODE_PRIVATE
        )
        bidRequestId = getSharedPreferences?.getInt("bidRequestId", 0)

        idToken = "Bearer ${getSharedPreferences?.getString("IdToken", "")}"
    }

    private fun quoteBriefApiCall() {
        getViewModel()?.getQuoteBrief(idToken, bidRequestId!!)
            ?.observe(viewLifecycleOwner, Observer { apiResponse ->

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

    override suspend fun tokenCallBack(idToken: String, caller: String) {
        withContext(Main) {
            quoteBriefApiCall()
        }
    }


    private fun apiTokenValidationQuoteBrief() {
        if (idToken.isNotEmpty()) {
            Log.d("TAG", "onResume: called")
            tokens.checkTokenExpiry(
                requireActivity().applicationContext as SMFApp,
                "quote_brief", idToken)
        }
    }

    fun setPendingQuoteBrief(response: QuoteBrief) {
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
}