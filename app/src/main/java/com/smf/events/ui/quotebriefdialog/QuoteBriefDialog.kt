package com.smf.events.ui.quotebriefdialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smf.events.R
import com.smf.events.SMFApp
import com.smf.events.base.BaseDialogFragment
import com.smf.events.databinding.QuoteBriefDialogBinding
import com.smf.events.helper.ApisResponse
import com.smf.events.helper.AppConstants
import com.smf.events.helper.SharedPreference
import com.smf.events.helper.Tokens
import com.smf.events.ui.quotebrief.model.QuoteBrief
import com.smf.events.ui.quotebriefdialog.model.Datas
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


class QuoteBriefDialog(var status: Int) :
    BaseDialogFragment<QuoteBriefDialogBinding, QuoteBriefDialogViewModel>(),
    Tokens.IdTokenCallBackInterface {

    var bidRequestId: Int? = 0
    lateinit var idToken: String
    var expand = false
    lateinit var bidStatus: String
    var serviceName: String = "null"
    var branchName: String = ""
    lateinit var file: File
    private var fileName: String? = null
    private var fileContent: String? = null
    private var fileSize: String? = null

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
        setIdTokenAndBidReqId()
    }

    override fun onStart() {
        super.onStart()
        apiTokenValidationQuoteBrief("quoteDetails")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mDataBinding?.quoteBriefDialogLayout?.visibility = View.INVISIBLE
        mDataBinding?.progressBar?.visibility = View.VISIBLE

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
    }

    // 2962 view Quotes
    private fun viewQuotes() {
        var status = false
        mDataBinding?.viewQuote?.setOnClickListener {
            mDataBinding?.quoteBriefDialogLayout?.visibility = View.GONE
            mDataBinding?.viewQuotes?.visibility = View.VISIBLE
            status = true
            mDataBinding?.txCatering?.text = "Quote details for $serviceName"
            apiTokenValidationQuoteBrief("viewQuotes")
            mDataBinding?.btnBack?.setOnClickListener {
                mDataBinding?.quoteBriefDialogLayout?.visibility = View.VISIBLE
                mDataBinding?.viewQuotes?.visibility = View.GONE

                onResume()
            }
        }

    }

    // 2962 Api call View Quotes
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getViewQuotes(idToken: String) {
        getViewModel().getViewQuote(idToken, status)
            .observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        Log.d(TAG, "getViewQuotes: ${apiResponse.response.data}")
                        val data = apiResponse.response.data
                        setViewQuote(data)
                    }
                    is ApisResponse.Error -> {
                        Log.d(TAG, "check token result: ${apiResponse.exception}")
                        mDataBinding?.progressBar?.visibility = View.INVISIBLE
                    }
                    else -> {
                    }
                }
            })
    }
   // 2962 Method to set the view Quotes
    private fun setViewQuote(data: Datas) {
        fileName = data.fileName
        fileSize = data.fileSize
        fileContent = data.fileContent
        // Set costing type
        mDataBinding?.costingType?.text = data.costingType
        // Set filename
        mDataBinding?.filenameTx?.text = data.fileName
        // Visible the Documents
        if (!data.fileContent.isNullOrEmpty()){
            mDataBinding?.filenameTx?.visibility=View.VISIBLE
            mDataBinding?.fileImg?.visibility=View.VISIBLE
            mDataBinding?.fileImgDelete?.visibility=View.VISIBLE
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
        if (!data.comment.isNullOrEmpty()) {
            mDataBinding?.etComments?.text = data.comment
        }

        mDataBinding?.fileImgDelete?.setOnClickListener {
            // generateNoteOnSD(context,fileName,fileContent)
            //  writeResponseBodyToDisk(fileName,fileContent,fileSize)
        }
    }
    override fun onResume() {
        super.onResume()
        // 2962
        mDataBinding?.txCatering?.text = "${serviceName}-${branchName}"
        mDataBinding?.btnBack?.setOnClickListener {
            backButtonClickListener()
        }
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
        mDataBinding?.progressBar?.visibility = View.INVISIBLE
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
        mDataBinding?.address?.text = "${response.data.serviceAddressDto.addressLine1}  " +
                "${response.data.serviceAddressDto.addressLine2}   " +
                "${response.data.serviceAddressDto.city}"
        mDataBinding?.customerRating?.text = "NA"

        mDataBinding?.progressBar?.visibility = View.INVISIBLE

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
        mDataBinding?.progressBar?.visibility = View.INVISIBLE
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
                    }
                    is ApisResponse.Error -> {
                        Log.d(TAG, "check token result: ${apiResponse.exception}")
                        mDataBinding?.progressBar?.visibility = View.INVISIBLE
                    }
                    else -> {
                    }
                }
            })
    }

    // 2904 Method for won bid service start flow
    fun widgetWonBid(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = AppConstants.BID_WON_SMALL
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
        mDataBinding?.txWonBid?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.txServiceProgress?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow3?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = AppConstants.SERVICE_IN_PROGRESS_SMALL
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    fun widgetServiceCloser(apiResponse: ApisResponse.Success<QuoteBrief>) {
        mDataBinding?.check2?.setImageResource(R.drawable.green_check)
        mDataBinding?.check3?.setImageResource(R.drawable.green_check)
        mDataBinding?.check4?.setImageResource(R.drawable.green_check)
        mDataBinding?.check5?.setImageResource(R.drawable.green_check)
        mDataBinding?.txWonBid?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.txServiceProgress?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.txServiceCompleted?.setTextColor(ContextCompat.getColor(
            context?.applicationContext!!, R.color.dark_font
        ))
        mDataBinding?.processflow2?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow5?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.processflow3?.setBackgroundResource(R.color.blue_event_id)
        mDataBinding?.spnBidAccepted?.text = AppConstants.SERVICE_COMPLETED
        setBidSubmitQuoteBrief(apiResponse.response)
    }

    // Call Back From Token Class
    @RequiresApi(Build.VERSION_CODES.R)
    override suspend fun tokenCallBack(idToken: String, caller: String) {
        if (caller == "viewQuotes") {
            withContext(Dispatchers.Main) {
                getViewQuotes(idToken)
            }
        } else {
            withContext(Dispatchers.Main) {
                quoteBriefApiCall(idToken)

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

//    @RequiresApi(Build.VERSION_CODES.R)
//    fun generateNoteOnSD(context: Context?, sFileName: String?, sBody: String?) {
//        val root:File = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString(),"text")
//        val filepath= getExternalStorageState(Environment.getStorageDirectory())
//        var fos: FileOutputStream? = null
//        try {
//            Log.d(TAG, "generateNoteOnSD: $filepath")
//            val writer = FileWriter(root)
//            writer.append("jai")
//            writer.flush()
//            writer.close()
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
//            fos?.close()
//        }

//        try {
//            val file =
//                File("")
//            if (!file.exists()) {
//                file.mkdirs()
//                if (sBody != null) {
//                    val attachment: String = parseBase64(sBody).toString()
//                    val byteArr = Base64.decode(attachment, Base64.DEFAULT)
//                    val f = File(file.absolutePath, sFileName)
//                    f.createNewFile()
//                    val fo = FileOutputStream(f)
//                    fo.write(byteArr)
//                    fo.close()
//                    Toast.makeText(requireContext(),
//                        "File downloaded ${file.absolutePath}",
//                        Toast.LENGTH_SHORT).show()
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        try {
//            val root:File = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString())
//            if (!root.exists()) {
//                root.mkdirs()
//            }
//
////            val pdfAsBytes: ByteArray = Base64.decode(sBody, Base64.DEFAULT)
////            Log.d(TAG, "generateNoteOnSD:$pdfAsBytes ${root.absolutePath} ")
////            val input: InputStream?=sBody?.byteInputStream(StandardCharsets.UTF_8)
////            var bytes = getBytes(input!!)
//            val data: ByteArray = Base64.decode(sBody, Base64.DEFAULT)
//            val fileContext = String(data, StandardCharsets.UTF_8)
//            val gpxfile = File(root, sFileName)
//            val writer = FileWriter(gpxfile)
//            writer.append("jai")
////            writer.flush()
//            writer.close()
//            Log.d(TAG, "generateNoteOnSD:$root ${root.absolutePath} ")
//            Toast.makeText(context, "Saved ${root.absolutePath}", Toast.LENGTH_SHORT).show()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
    //  }
//    fun parseBase64(base64: String?): String? {
//        try {
//            val pattern: Pattern =
//                Pattern.compile("((?<=base64,).*\\s*)", Pattern.DOTALL or Pattern.MULTILINE)
//            val matcher: Matcher = pattern.matcher(base64)
//            return if (matcher.find()) {
//                matcher.group().toString()
//            } else {
//                ""
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return ""
//    }
//
//    @Throws(IOException::class)
//    fun getBytes(inputStream: InputStream): ByteArray? {
//        val byteBuffer = ByteArrayOutputStream()
//        val bufferSize = 1024
//        val buffer = ByteArray(bufferSize)
//        var len = 0
//        while (inputStream.read(buffer).also { len = it } != -1) {
//            byteBuffer.write(buffer, 0, len)
//        }
//        return byteBuffer.toByteArray()
//    }
//    private fun writeResponseBodyToDisk(
//        fileName: String?,
//        fileContent: String?,
//        fileSize: String?,
//    ): Boolean {
//        Toast.makeText(requireContext(), "download", Toast.LENGTH_SHORT).show()
//
//        return try {
//            // todo change the file location/name according to your needs
//            val futureStudioIconFile: File =
//                File(requireContext().getExternalFilesDir(null).toString() + File.separator + "Future Studio Icon.png")
//            var inputStream: InputStream? = null
//            var outputStream: OutputStream? = null
//            try {
//                val fileReader = ByteArray(4096)
//                val fileSize = fileSize
//                var fileSizeDownloaded: Long = 0
//                inputStream = body.inputStream()
//                outputStream = FileOutputStream(futureStudioIconFile)
//                while (true) {
//                    val read = inputStream.read(fileReader)
//                    if (read == -1) {
//                        break
//                    }
//                    outputStream.write(fileReader, 0, read)
//                    fileSizeDownloaded += read.toLong()
//                    Log.d(QuoteDetailsDialog.TAG, "file download: $fileSizeDownloaded of $fileSize")
//                }
//                outputStream.flush()
//                true
//            } catch (e: IOException) {
//                false
//            } finally {
//                inputStream?.close()
//                if (outputStream != null) {
//                    outputStream.close()
//                }
//            }
//        } catch (e: IOException) {
//            false
//        }
//    }
}