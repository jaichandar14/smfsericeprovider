package com.smf.events.ui.quotebriefdialog

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.smf.events.MainActivity
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.QuoteBriefDialogBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.SharedPreference.Companion.isDialogShown
import com.smf.events.helper.Tokens
import com.smf.events.rxbus.RxBus
import com.smf.events.rxbus.RxEvent
import com.smf.events.ui.intiateservicedialog.InitiateServiceDialog
import com.smf.events.ui.quotebrief.model.QuoteBrief
import com.smf.events.ui.quotebriefdialog.model.Datas
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class QuoteBriefDialog() : BaseDialogFragment<QuoteBriefDialogBinding, QuoteBriefDialogViewModel>(),
    Tokens.IdTokenCallBackInterface, InitiateServiceDialog.CallBackInterface {

    var TAG = this::class.java.name
    var num: Int = 0
    var bidRequestId: Int? = 0
    var eventId: Int? = 0
    var eventDescriptionId: Int? = 0
    lateinit var idToken: String
    var expand = false
    lateinit var bidStatus: String
    var serviceName: String = "null"
    var branchName: String = ""
    lateinit var file: File
    private var fileName: String? = null
    private var fileContent: String? = null
    private var fileSize: String? = null
    var isViewQuoteClicked = false
    var STORAGE_PERMISSION_CODE = 1
    var bidRequestIdUpdated: Int? = 0
    private lateinit var dialogDisposable: Disposable
    private var currentUI: String? = null

    companion object {
        const val TAG = "CustomDialogFragment"
        fun newInstance(): QuoteBriefDialog {
            return QuoteBriefDialog()
        }
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
        isDialogShown = true
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
        // Initialize Local Variables
        setIdTokenAndBidReqId()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgress()
        // token CallBackInterface
        tokens.setCallBackInterface(this)
        // Back Button Pressed
        mDataBinding?.btnBack?.setOnClickListener {
            backButtonClickListener()
        }
        // Expandable view
        getViewModel().expandableView(mDataBinding, expand)
        // 2962 View Quotes
        viewQuotes()
        dialogDisposable = RxBus.listen(RxEvent.DenyStorage::class.java).subscribe {
            checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE
            )
        }

        dialogDisposable = RxBus.listen(RxEvent.InternetStatus::class.java).subscribe {
            Log.d(TAG, "onViewCreated: observer QuoteBrief dialog")
            if (currentUI == "viewQuotes") {
                apiTokenValidationQuoteBrief("viewQuotes")
            } else {
                // Quote details api call
                apiTokenValidationQuoteBrief("quoteDetails")
            }
        }
        // Quote details api call
        apiTokenValidationQuoteBrief("quoteDetails")

        parentFragmentManager.setFragmentResultListener("5", viewLifecycleOwner,
            FragmentResultListener { _: String, _: Bundle -> apiTokenValidationQuoteBrief("quoteDetails")
            })
    }

    // 2962 view Quotes
    private fun viewQuotes() {
        mDataBinding?.viewQuote?.setOnClickListener {
            mDataBinding?.quoteBriefDialogLayout?.visibility = View.GONE
            mDataBinding?.progressBar?.visibility = View.VISIBLE
            isViewQuoteClicked = true
            mDataBinding?.txCateringViewq?.text = "Quote details for $serviceName"
            mDataBinding?.txCateringViewq?.visibility = View.VISIBLE
            mDataBinding?.txCatering?.visibility = View.GONE
            apiTokenValidationQuoteBrief("viewQuotes")
            mDataBinding?.btnBack?.setOnClickListener {
                mDataBinding?.quoteBriefDialogLayout?.visibility = View.VISIBLE
                mDataBinding?.txCateringViewq?.visibility = View.GONE
                mDataBinding?.txCatering?.visibility = View.VISIBLE
                mDataBinding?.viewQuotes?.visibility = View.GONE
                isViewQuoteClicked = false
                onResume()
            }
        }
    }

    // 2962 Api call View Quotes
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getViewQuotes(idToken: String) {
        Log.d(TAG, "showDialog: $bidRequestId")
        bidRequestId?.let {
            getViewModel().getViewQuote(idToken, it)
                .observe(viewLifecycleOwner, Observer { apiResponse ->
                    when (apiResponse) {
                        is ApisResponse.Success -> {
                            Log.d(TAG, "getViewQuotes: ${apiResponse.response.data}")
                            val data = apiResponse.response.data
                            setViewQuote(data)
                            mDataBinding?.viewQuotes?.visibility = View.VISIBLE
                            mDataBinding?.progressBar?.visibility = View.INVISIBLE
                        }
                        is ApisResponse.CustomError -> {
                            Log.d(TAG, "check token result: ${apiResponse.message}")
                            showToastMessage(
                                apiResponse.message,
                                Snackbar.LENGTH_LONG,
                                AppConstants.PLAIN_SNACK_BAR
                            )
                            hideProgress()
                        }
                        is ApisResponse.InternetError -> {
                            (requireActivity() as MainActivity).showInternetDialog(apiResponse.message)
                            hideProgress()
                        }
                        else -> {}
                    }
                })
        }
    }

    // 2962 Method to set the view Quotes
    private fun setViewQuote(data: Datas) {
        fileName = data.fileName
        fileSize = data.fileSize
        fileContent = data.fileContent
        // Set costing type
        mDataBinding?.costingType?.text = data.costingType
        // Set filename
        val iend: Int? = fileName?.lastIndexOf(".")
        var subString = String()
        if (iend != -1) {
            subString = iend?.let { fileName!!.substring(0, it) }.toString() //this will give abc
        }
        //3473 String out of bound exception in file name
        try {
            var fileTypepath = fileName?.substring(fileName!!.lastIndexOf("."))
            mDataBinding?.filenameTx?.text = subString.take(10) + "..." + fileTypepath
        } catch (e: Exception) {
            mDataBinding?.filenameTx?.text = AppConstants.INVALIDDOC
        }
        // Visible the Documents
        if (!data.fileContent.isNullOrEmpty()) {
            mDataBinding?.txQuoteDetails?.visibility = View.VISIBLE
            mDataBinding?.filenameTx?.visibility = View.VISIBLE
            mDataBinding?.fileImg?.visibility = View.VISIBLE
            mDataBinding?.fileImgDelete?.visibility = View.VISIBLE
        }
        // Showing the currency type
        if (data.currencyType.isNullOrEmpty()) {
            if (data.costingType == "Bidding") {
                mDataBinding?.costEstimationAmount?.text = data.latestBidValue
            } else mDataBinding?.costEstimationAmount?.text = "$ " + data.cost
        } else {
            if (data.costingType == "Bidding") {
                val currencyType = when (data.currencyType) {
                    "USD($)" -> "$ "
                    "GBP(\u00a3)" -> "\u00a3 "
                    "INR(\u20B9)" -> "₹ "
                    else -> {
                        "$ "
                    }
                }
                mDataBinding?.costEstimationAmount?.text =
                    currencyType + data.latestBidValue
            } else mDataBinding?.costEstimationAmount?.text = "$ " + data.cost
        }
        if (data.comment.isNullOrEmpty().not()) {
            mDataBinding?.etComments?.text = data.comment
        }

        mDataBinding?.fileImgDelete?.tag = data.fileContent

        mDataBinding?.fileImgDelete?.setOnClickListener {
            //   saveFileNew(it.tag.toString(), fileName)
            checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                STORAGE_PERMISSION_CODE
            )
        }

    }

    private fun saveFileNew(newContent: String?, fileName: String?) {
        val folder = File("/storage/emulated/0/Download", "")
        var subString: String? = null

        if (!folder.exists()) {
            folder.mkdirs()
        } else {
            var iend: Int = fileName!!.lastIndexOf(".")
            if (iend != -1) {
                subString = fileName.substring(0, iend) //this will give abc
            }
            //3473 String out of bound exception in file name
            try {
                var fileTypepath = fileName.substring(fileName.lastIndexOf("."))
                createFile(subString, fileTypepath, newContent)
            } catch (e: Exception) {
                showToastMessage(
                    getString(R.string.invaild_file_download),
                    Snackbar.LENGTH_LONG, AppConstants.PLAIN_SNACK_BAR
                )
            }
        }

    }

    // Creating a non repeatable file
    private fun createFile(prefix: String?, fileTypepath: String, newContent: String?) {
        var filename: String? = null
        if (num == 0) {
            filename = "$prefix$fileTypepath"
        } else {
            filename = "$prefix($num)$fileTypepath" //create the correct filename
        }
        val myFile = File("/storage/emulated/0/Download", filename)
        Log.d(TAG, "saveFileNew: ${myFile.absoluteFile.name}")
        try {
            if (myFile.exists().not()) {
                if (num == 0) {
                    filename = "$prefix$fileTypepath"
                } else {
                    filename = "$prefix($num)$fileTypepath" //create the correct filename
                }
                Log.d(TAG, "saveFileNew2: ${num}${filename}")
                val myFile1 = File("/storage/emulated/0/Download", filename)
                myFile1.createNewFile()
                val pdfAsBytes: ByteArray = Base64.decode(newContent, 0)
                val os = FileOutputStream(myFile1, false)
                os.write(pdfAsBytes)
                os.flush()
                os.close()
                //  showToast("File Downloaded  ${myFile1.absoluteFile.name}")
                showToastMessage(
                    "File Downloaded  ${myFile1.absoluteFile.name}",
                    Snackbar.LENGTH_LONG, AppConstants.PLAIN_SNACK_BAR
                )
            } else {
                num++ //increase the file index
                createFile(
                    prefix,
                    fileTypepath,
                    newContent
                ) //simply call this method again with the same prefix
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        RxBus.publish(RxEvent.ChangingNavDialog(dialog))
        // 2962
        if (isViewQuoteClicked) {
            mDataBinding?.viewQuotes?.visibility = View.VISIBLE
            mDataBinding?.quoteBriefDialogLayout?.visibility = View.GONE
            mDataBinding?.txCateringViewq?.text = "Quote details for $serviceName"
            mDataBinding?.txCateringViewq?.visibility = View.VISIBLE
            mDataBinding?.txCatering?.visibility = View.GONE
        } else {
            mDataBinding?.viewQuotes?.visibility = View.GONE
            //   mDataBinding?.txCatering?.text = "Quote details for $serviceName"
            mDataBinding?.txCateringViewq?.visibility = View.GONE
            mDataBinding?.txCatering?.visibility = View.VISIBLE
            mDataBinding?.btnBack?.setOnClickListener {
                backButtonClickListener()
            }
            // backButtonClickListener()
        }
        mDataBinding?.btnBack?.setOnClickListener {
            backButtonClickListener()
        }
        mDataBinding?.txCatering?.text = "${serviceName}-${branchName}"

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
        mDataBinding?.txJobTitle?.text = response.data.eventName
        mDataBinding?.txCatering?.text = "${response.data.serviceName}-${response.data.branchName}"
        // 2835
        serviceName = response.data.serviceName
        branchName = response.data.branchName
        mDataBinding?.serviceName?.text = response.data.serviceName
        mDataBinding?.branchName?.text = response.data.branchName
        mDataBinding?.txJobTitle?.text = response.data.eventName
        // 2962
        mDataBinding?.txJobTitleVo?.text = response.data.eventName
        mDataBinding?.serviceNameVo?.text = response.data.serviceName
        mDataBinding?.branchNameVo?.text = response.data.branchName
        mDataBinding?.txJobTitleVo?.text = response.data.eventName
        mDataBinding?.txJobIdnumVo?.text = response.data.eventServiceDescriptionId.toString()
        // 2354
        val currencyType = setCurrencyType(response)
        if (response.data.costingType == AppConstants.BIDDING) {
            mDataBinding?.txJobAmount?.text = "$currencyType${response.data.latestBidValue}"
        } else {
            mDataBinding?.txJobAmount?.text = "$currencyType${response.data.cost}"
        }
        mDataBinding?.txJobIdnum?.text = response.data.eventServiceDescriptionId.toString()
        mDataBinding?.txEventdateValue?.text = (response.data.eventDate)
        mDataBinding?.txBidProposalDateValue?.text = (response.data.bidRequestedDate)
        mDataBinding?.txCutOffDateValue?.text = (response.data.biddingCutOffDate)
        mDataBinding?.serviceDateValue?.text = (response.data.serviceDate)
        mDataBinding?.paymentStatusValue?.text = "NA"
        mDataBinding?.servicedBy?.text = "NA"
        var address = response.data.serviceAddressDto.addressLine1 +
                " ${response.data.serviceAddressDto.addressLine2}" +
                " ${response.data.serviceAddressDto.city}"
        mDataBinding?.address?.text = address
        mDataBinding?.customerRating?.text = "NA"
        mDataBinding?.reviewComment?.text = "NA"
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
        mDataBinding?.txEventdateValue?.text = (response.data.eventDate)
        mDataBinding?.txBidProposalDateValue?.text = (response.data.bidRequestedDate)
        mDataBinding?.txCutOffDateValue?.text = (response.data.biddingCutOffDate)
        mDataBinding?.serviceDateValue?.text = (response.data.serviceDate)
        mDataBinding?.paymentStatusValue?.text = "NA"
        mDataBinding?.servicedBy?.text = "NA"
        mDataBinding?.address?.text = "${response.data.serviceAddressDto.addressLine1}  " +
                "${response.data.serviceAddressDto.addressLine2}   " +
                "${response.data.serviceAddressDto.city}"
        mDataBinding?.customerRating?.text = "NA"
    }

    // Setting IDToken
    private fun setIdTokenAndBidReqId() {
        bidRequestIdUpdated = sharedPreference.getInt(SharedPreference.BID_REQUEST_ID_UPDATED)
        bidRequestId = sharedPreference.getInt(SharedPreference.BID_REQUEST_ID)
        eventId = sharedPreference.getInt(SharedPreference.EVENT_ID)
        eventDescriptionId = sharedPreference.getInt(SharedPreference.EVENT_DESCRIPTION_ID)
        idToken = "${AppConstants.BEARER} ${sharedPreference.getString(SharedPreference.ID_Token)}"
    }

    // 2904 Get Api Call for getting the Quote Brief
    private fun quoteBriefApiCall(idToken: String) {
        Log.d("TAG", "showDialog: ${bidRequestIdUpdated} ${bidRequestId}")
        bidRequestId?.let {
            getViewModel().getQuoteBrief(idToken, it)
                .observe(viewLifecycleOwner, Observer { apiResponse ->
                    when (apiResponse) {
                        is ApisResponse.Success -> {
                            bidStatus = apiResponse.response.data.bidStatus
                            when (bidStatus) {
                                AppConstants.BID_SUBMITTED -> {
                                    setBidSubmitQuoteBrief(apiResponse.response)
                                    mDataBinding?.spnBidAccepted?.text =
                                        AppConstants.BIDDING_IN_PROGRESS
                                }
                                AppConstants.PENDING_FOR_QUOTE -> setPendingQuoteBrief(apiResponse.response)
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
                            hideProgress()
                        }
                        is ApisResponse.CustomError -> {
                            Log.d(TAG, "check token result: ${apiResponse.message}")
                            mDataBinding?.progressBar?.visibility = View.INVISIBLE
                            showToastMessage(
                                apiResponse.message,
                                Snackbar.LENGTH_LONG,
                                AppConstants.PLAIN_SNACK_BAR
                            )
                            hideProgress()
                        }
                        is ApisResponse.InternetError -> {
                            (requireActivity() as MainActivity).showInternetDialog(apiResponse.message)
                            hideProgress()
                        }
                        else -> {}
                    }
                })
        }
    }

    // 2904 Method for won bid service start flow
    private fun widgetWonBid(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = AppConstants.BID_WON_SMALL
        val currentDateValue = LocalDateTime.now()
        val formatterDay = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val formattedDay: String = currentDateValue.format(formatterDay)
        // Restricting the start service flow based previous and today service Date
       if (apiResponse.response.data.serviceDate <= formattedDay) {
           mDataBinding?.apply {
               btnStartService.apply {
                   Log.d(TAG, "widgetWonBid:$serviceDate")
                   //  Hiding  Confirm service feature later we need to Visible implement
                   visibility = View.GONE
                   setOnClickListener {
                       InitiateServiceDialog.newInstance(
                           bidRequestId,
                           eventId,
                           eventDescriptionId,
                           AppConstants.WON_BID
                       ).show(
                           requireActivity().supportFragmentManager,
                           InitiateServiceDialog.TAG
                       )
                   }
               }
               btnRejectService.apply {
                   //  Hiding  Confirm service feature later we need to Visible implement
                   visibility = View.GONE
               }
           }
       }
        mDataBinding?.txWonBid?.setTextColor(
            ContextCompat.getColor(
                context?.applicationContext!!, R.color.dark_font
            )
        )
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.inprogress)
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    // 2904 Method for Initiate closer flow
    private fun widgetServiceProgress(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.green_check)
        mDataBinding?.check4?.setImageResource(R.drawable.inprogress)
        mDataBinding?.txWonBid?.setTextColor(
            ContextCompat.getColor(
                context?.applicationContext!!, R.color.dark_font
            )
        )
        mDataBinding?.txServiceProgress?.setTextColor(
            ContextCompat.getColor(
                context?.applicationContext!!, R.color.dark_font
            )
        )
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow3?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = AppConstants.SERVICE_IN_PROGRESS_SMALL
        mDataBinding?.apply {
            //  Hiding  Initiate Closer feature later we need to Visible implement
            btnStartService.apply {
                visibility = View.GONE
                text = context.getString(R.string.initiate_close_txt)
                setOnClickListener {
                    InitiateServiceDialog.newInstance(
                        bidRequestId,
                        eventId,
                        eventDescriptionId,
                        AppConstants.SERVICE_DONE
                    ).show(
                        requireActivity().supportFragmentManager,
                        InitiateServiceDialog.TAG
                    )
                }
            }
        }
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    fun widgetServiceCloser(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.green_check)
        mDataBinding?.check4?.setImageResource(R.drawable.green_check)
        mDataBinding?.check5?.setImageResource(R.drawable.green_check)
        mDataBinding?.txWonBid?.setTextColor(
            ContextCompat.getColor(
                context?.applicationContext!!, R.color.dark_font
            )
        )
        mDataBinding?.txServiceProgress?.setTextColor(
            ContextCompat.getColor(
                context?.applicationContext!!, R.color.dark_font
            )
        )
        mDataBinding?.txServiceCompleted?.setTextColor(
            ContextCompat.getColor(
                context?.applicationContext!!, R.color.dark_font
            )
        )
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow5?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow3?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = AppConstants.SERVICE_COMPLETED
        mDataBinding?.apply {
            btnStartService.text=getString(R.string.write_review_txt)

        }
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    // Call Back From Token Class
    @RequiresApi(Build.VERSION_CODES.R)
    override suspend fun tokenCallBack(idToken: String, caller: String) {
        if (caller == "viewQuotes") {
            withContext(Dispatchers.Main) {
                currentUI = "viewQuotes"
                getViewQuotes(idToken)
            }
        } else {
            withContext(Dispatchers.Main) {
                currentUI = "quoteDetails"
                quoteBriefApiCall(idToken)
            }
        }
    }

    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            // Requesting the permission
            requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            //   Toast.makeText(requireContext(), "Permission already granted", Toast.LENGTH_SHORT).show()
            saveFileNew(mDataBinding?.fileImgDelete?.tag.toString(), fileName)
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            when (it) {
                true -> {
                    saveFileNew(mDataBinding?.fileImgDelete?.tag.toString(), fileName)
                }
                false -> {
                    // showToast(resources.getString(R.string.Without_giving_permission_you_))
                    showToastMessage(
                        resources.getString(R.string.Without_giving_permission_you_),
                        Snackbar.LENGTH_LONG, AppConstants.PLAIN_SNACK_BAR
                    )
//                DeselectingDialogFragment.newInstance(
//                    AppConstants.DAY,
//                    "Deny",
//                    "timeSlot",
//                    "currentMonth",
//                    1,
//                    "fromDate",
//                    "toDate", null
//                )
//                    .show(
//                        (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
//                        DeselectingDialogFragment.TAG
//                    )
                }

            }
        }

    // Api Token Validation For Quote Brief Api Call
    private fun apiTokenValidationQuoteBrief(s: String) {
        tokens.checkTokenExpiry(
            requireActivity().applicationContext as SMFApp,
            s, idToken
        )
    }

    private fun showProgress() {
        mDataBinding?.quoteBriefDialog?.visibility = View.INVISIBLE
        mDataBinding?.progressBar?.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        mDataBinding?.quoteBriefDialog?.visibility = View.VISIBLE
        mDataBinding?.progressBar?.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        isDialogShown = false
        if (dialogDisposable.isDisposed.not()) dialogDisposable.dispose()
    }

    override fun showDialog(status: Boolean) {
        if (status == true) {
            dismiss()
            newInstance().show(
                (context as androidx.fragment.app.FragmentActivity).supportFragmentManager,
                QuoteBriefDialog.TAG
            )
        }
    }
}